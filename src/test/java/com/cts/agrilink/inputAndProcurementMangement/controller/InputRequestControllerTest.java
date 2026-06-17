package com.cts.agrilink.inputAndProcurementMangement.controller;

import com.cts.agrilink.inputAndProcurementMangement.dto.*;
import com.cts.agrilink.inputAndProcurementMangement.exception.GlobalExceptionHandler;
import com.cts.agrilink.inputAndProcurementMangement.exception.ResourceNotFoundException;
import com.cts.agrilink.inputAndProcurementMangement.exception.StateConflictException;
import com.cts.agrilink.inputAndProcurementMangement.model.InputRequest;
import com.cts.agrilink.inputAndProcurementMangement.service.InputRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InputRequestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InputRequestService inputRequestService;

    @InjectMocks
    private InputRequestController inputRequestController;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private InputRequest sampleRequest;

    private static final String BASE_URL = "/agriLink/procurementManagement/input-requests";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inputRequestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleRequest = InputRequest.builder()
                .requestId(1L).farmerId(100L).inputId(10L)
                .quantityRequested(50).requestDate(LocalDate.now())
                .assignedCentreId(5L).actualPrice(1000.0).status("Requested")
                .build();
    }

    // ─── GET /input-requests ─────────────────────────────────────────────────────

    @Test
    void getAllRequests_returns200WithList() throws Exception {
        when(inputRequestService.getAllRequests()).thenReturn(List.of(sampleRequest));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requestId").value(1))
                .andExpect(jsonPath("$[0].status").value("Requested"));
    }

    // ─── GET /input-requests/{requestId} ────────────────────────────────────────

    @Test
    void getRequestById_existingId_returns200() throws Exception {
        when(inputRequestService.getRequestById(1L)).thenReturn(sampleRequest);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.farmerId").value(100));
    }

    @Test
    void getRequestById_nonExistingId_returns404() throws Exception {
        when(inputRequestService.getRequestById(99L))
                .thenThrow(new ResourceNotFoundException("Input request not found with ID: 99"));

        mockMvc.perform(get(BASE_URL + "/99"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /input-requests/farmer/{farmerId} ───────────────────────────────────

    @Test
    void getRequestsByFarmer_returns200WithList() throws Exception {
        when(inputRequestService.getRequestsByFarmer(100L)).thenReturn(List.of(sampleRequest));

        mockMvc.perform(get(BASE_URL + "/farmer/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].farmerId").value(100));
    }

    // ─── GET /input-requests/status/{status} ─────────────────────────────────────

    @Test
    void getRequestsByStatus_validStatus_returns200() throws Exception {
        when(inputRequestService.getRequestsByStatus("Requested")).thenReturn(List.of(sampleRequest));

        mockMvc.perform(get(BASE_URL + "/status/Requested"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("Requested"));
    }

    @Test
    void getRequestsByStatus_invalidStatus_returns400() throws Exception {
        when(inputRequestService.getRequestsByStatus("Pending"))
                .thenThrow(new IllegalArgumentException("Invalid status: Pending"));

        mockMvc.perform(get(BASE_URL + "/status/Pending"))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /input-requests/centre/{centreId} ───────────────────────────────────

    @Test
    void getRequestsByCentre_returns200() throws Exception {
        when(inputRequestService.getRequestsByCentre(5L)).thenReturn(List.of(sampleRequest));

        mockMvc.perform(get(BASE_URL + "/centre/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].assignedCentreId").value(5));
    }

    // ─── POST /input-requests ────────────────────────────────────────────────────

    @Test
    void submitRequest_validDto_returns201() throws Exception {
        InputProcurementRequestDTO dto = new InputProcurementRequestDTO(
                100L, 10L, 50, LocalDate.now(), 5L, 1000.0);
        when(inputRequestService.submitRequest(any()))
                .thenReturn(new MessageResponseDTO("Input request submitted successfully"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Input request submitted successfully"));
    }

    @Test
    void submitRequest_missingFarmerId_returns400() throws Exception {
        InputProcurementRequestDTO dto = new InputProcurementRequestDTO(
                null, 10L, 50, LocalDate.now(), 5L, 1000.0);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ─── PUT /input-requests/{requestId} ────────────────────────────────────────

    @Test
    void updateRequest_existingId_returns200() throws Exception {
        UpdateInputRequestDTO dto = new UpdateInputRequestDTO(80, 7L, 1200.0);
        when(inputRequestService.updateRequest(eq(1L), any()))
                .thenReturn(new MessageResponseDTO("Input request updated successfully"));

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Input request updated successfully"));
    }

    // ─── PUT /input-requests/{requestId}/approve ─────────────────────────────────

    @Test
    void approveRequest_validTransition_returns200() throws Exception {
        ApproveRequestDTO dto = new ApproveRequestDTO(5L, 1000.0, "officer_id_45");
        when(inputRequestService.approveRequest(eq(1L), any()))
                .thenReturn(new MessageResponseDTO("Input request approved successfully"));

        mockMvc.perform(put(BASE_URL + "/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Input request approved successfully"));
    }

    @Test
    void approveRequest_invalidTransition_returns409() throws Exception {
        ApproveRequestDTO dto = new ApproveRequestDTO(5L, 1000.0, "officer_id_45");
        when(inputRequestService.approveRequest(eq(2L), any()))
                .thenThrow(new StateConflictException("Request cannot be approved. Current status: Approved"));

        mockMvc.perform(put(BASE_URL + "/2/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    // ─── PUT /input-requests/{requestId}/dispatch ────────────────────────────────

    @Test
    void dispatchRequest_validTransition_returns200() throws Exception {
        DispatchRequestDTO dto = new DispatchRequestDTO("staff_id_9", LocalDate.now());
        when(inputRequestService.dispatchRequest(eq(2L), any()))
                .thenReturn(new MessageResponseDTO("Dispatched successfully"));

        mockMvc.perform(put(BASE_URL + "/2/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Dispatched successfully"));
    }

    @Test
    void dispatchRequest_invalidTransition_returns409() throws Exception {
        DispatchRequestDTO dto = new DispatchRequestDTO("staff_id_9", LocalDate.now());
        when(inputRequestService.dispatchRequest(eq(1L), any()))
                .thenThrow(new StateConflictException("Request cannot be dispatched. Current status: Requested"));

        mockMvc.perform(put(BASE_URL + "/1/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    // ─── PUT /input-requests/{requestId}/deliver ─────────────────────────────────

    @Test
    void deliverRequest_validTransition_returns200() throws Exception {
        DeliverRequestDTO dto = new DeliverRequestDTO(LocalDate.now(), "farmer_id_501");
        when(inputRequestService.deliverRequest(eq(3L), any()))
                .thenReturn(new MessageResponseDTO("Delivered to farmer successfully"));

        mockMvc.perform(put(BASE_URL + "/3/deliver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Delivered to farmer successfully"));
    }

    @Test
    void deliverRequest_invalidTransition_returns409() throws Exception {
        DeliverRequestDTO dto = new DeliverRequestDTO(LocalDate.now(), "farmer_id_501");
        when(inputRequestService.deliverRequest(eq(1L), any()))
                .thenThrow(new StateConflictException("Request cannot be delivered. Current status: Requested"));

        mockMvc.perform(put(BASE_URL + "/1/deliver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    // ─── PUT /input-requests/{requestId}/cancel ──────────────────────────────────

    @Test
    void cancelRequest_validTransition_returns200() throws Exception {
        CancelRequestDTO dto = new CancelRequestDTO("Farmer withdrew due to crop change");
        when(inputRequestService.cancelRequest(eq(1L), any()))
                .thenReturn(new MessageResponseDTO("Request cancelled successfully"));

        mockMvc.perform(put(BASE_URL + "/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Request cancelled successfully"));
    }

    @Test
    void cancelRequest_dispatchedStatus_returns409() throws Exception {
        CancelRequestDTO dto = new CancelRequestDTO("Farmer withdrew due to crop change");
        when(inputRequestService.cancelRequest(eq(3L), any()))
                .thenThrow(new StateConflictException("Cannot cancel request after dispatch. Current status: Dispatched"));

        mockMvc.perform(put(BASE_URL + "/3/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    // ─── DELETE /input-requests/{requestId} ──────────────────────────────────────

    @Test
    void deleteRequest_existingId_returns200() throws Exception {
        when(inputRequestService.deleteRequest(1L))
                .thenReturn(new MessageResponseDTO("Input request record deleted successfully"));

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Input request record deleted successfully"));
    }

    @Test
    void deleteRequest_nonExistingId_returns404() throws Exception {
        when(inputRequestService.deleteRequest(99L))
                .thenThrow(new ResourceNotFoundException("Input request not found with ID: 99"));

        mockMvc.perform(delete(BASE_URL + "/99"))
                .andExpect(status().isNotFound());
    }
}
