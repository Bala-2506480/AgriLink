package com.cts.agrilink.inputAndProcurementMangement.integration;

import com.cts.agrilink.inputAndProcurementMangement.dto.*;
import com.cts.agrilink.inputAndProcurementMangement.model.AgriInput;
import com.cts.agrilink.inputAndProcurementMangement.repository.AgriInputRepository;
import com.cts.agrilink.inputAndProcurementMangement.repository.InputRequestRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration test: drives the FULL stack
 * (HTTP -> Controller -> Service -> Repository -> H2 database)
 * with nothing mocked. Verifies that the layers wire together and that the
 * request lifecycle works across real persistence.
 */
@SpringBootTest
@ActiveProfiles("test")
class InputProcurementIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AgriInputRepository agriInputRepository;

    @Autowired
    private InputRequestRepository inputRequestRepository;

    // Built manually (this project has no ObjectMapper bean); JavaTimeModule handles LocalDate.
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    private static final String INPUTS = "/agriLink/procurementManagement/inputs";
    private static final String REQUESTS = "/agriLink/procurementManagement/input-requests";

    private Long inputId;

    @BeforeEach
    void setUp() {
        // Build MockMvc over the REAL application context (full filter/advice chain).
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Clean slate, then seed one catalog item that requests can reference.
        inputRequestRepository.deleteAll();
        agriInputRepository.deleteAll();
        AgriInput saved = agriInputRepository.save(AgriInput.builder()
                .name("Urea").category("Fertiliser").unit("Kg")
                .pricePerUnit(20.0).subsidisedPrice(15.0)
                .availableStock(500).status("Available")
                .build());
        inputId = saved.getInputId();
    }

    @Test
    void fullLifecycle_submitApproveDispatchDeliver_persistsEachTransition() throws Exception {
        // 1. Farmer submits a request through the real HTTP endpoint -> 201 Created
        InputProcurementRequestDTO submit = new InputProcurementRequestDTO(
                100L, inputId, 50, LocalDate.now(), 5L, 1000.0);
        mockMvc.perform(post(REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Input request submitted successfully"));

        // The row really landed in the database.
        assertThat(inputRequestRepository.findAll()).hasSize(1);
        Long requestId = inputRequestRepository.findAll().get(0).getRequestId();
        assertThat(requestId).isNotNull();

        // 2. Approve -> 200, status persisted as "Approved"
        ApproveRequestDTO approve = new ApproveRequestDTO(5L, 1000.0, "officer_id_45");
        mockMvc.perform(put(REQUESTS + "/" + requestId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approve)))
                .andExpect(status().isOk());
        assertThat(inputRequestRepository.findById(requestId).orElseThrow().getStatus())
                .isEqualTo("Approved");

        // 3. Dispatch -> 200, status "Dispatched"
        DispatchRequestDTO dispatch = new DispatchRequestDTO("staff_id_9", LocalDate.now());
        mockMvc.perform(put(REQUESTS + "/" + requestId + "/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dispatch)))
                .andExpect(status().isOk());
        assertThat(inputRequestRepository.findById(requestId).orElseThrow().getStatus())
                .isEqualTo("Dispatched");

        // 4. Deliver -> 200, status "Delivered"
        DeliverRequestDTO deliver = new DeliverRequestDTO(LocalDate.now(), "farmer_id_501");
        mockMvc.perform(put(REQUESTS + "/" + requestId + "/deliver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deliver)))
                .andExpect(status().isOk());

        // 5. Final read back through the API confirms the end state.
        mockMvc.perform(get(REQUESTS + "/" + requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Delivered"))
                .andExpect(jsonPath("$.receivedBy").value("farmer_id_501"));
    }

    @Test
    void illegalTransition_dispatchWhileRequested_returns409_andStatusUnchanged() throws Exception {
        // Submit a request (status = Requested) ...
        InputProcurementRequestDTO submit = new InputProcurementRequestDTO(
                101L, inputId, 20, LocalDate.now(), 6L, 400.0);
        mockMvc.perform(post(REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submit)))
                .andExpect(status().isCreated());
        Long requestId = inputRequestRepository.findAll().get(0).getRequestId();

        // ... then try to dispatch it without approving first -> 409 Conflict
        DispatchRequestDTO dispatch = new DispatchRequestDTO("staff_id_9", LocalDate.now());
        mockMvc.perform(put(REQUESTS + "/" + requestId + "/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dispatch)))
                .andExpect(status().isConflict());

        // The state machine protected the data: still "Requested" in the DB.
        assertThat(inputRequestRepository.findById(requestId).orElseThrow().getStatus())
                .isEqualTo("Requested");
    }

    @Test
    void submitForUnknownInput_returns404() throws Exception {
        InputProcurementRequestDTO submit = new InputProcurementRequestDTO(
                100L, 999999L, 10, LocalDate.now(), 5L, 500.0);
        mockMvc.perform(post(REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submit)))
                .andExpect(status().isNotFound());

        assertThat(inputRequestRepository.findAll()).isEmpty();
    }

    @Test
    void catalogCreatedViaApi_isQueryableViaApi() throws Exception {
        // Create a second catalog item purely through HTTP, then read it back.
        AgriInputRequestDTO dto = new AgriInputRequestDTO(
                "DAP", "Fertiliser", "Kg", 30.0, 22.0, 200, "Available");
        mockMvc.perform(post(INPUTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        String body = mockMvc.perform(get(INPUTS + "/category/Fertiliser"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode arr = objectMapper.readTree(body);
        assertThat(arr).hasSizeGreaterThanOrEqualTo(2); // seeded Urea + created DAP
    }
}
