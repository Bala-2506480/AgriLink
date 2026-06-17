package com.cts.agrilink.inputAndProcurementMangement.controller;

import com.cts.agrilink.inputAndProcurementMangement.dto.AgriInputRequestDTO;
import com.cts.agrilink.inputAndProcurementMangement.dto.MessageResponseDTO;
import com.cts.agrilink.inputAndProcurementMangement.dto.UpdateInputDTO;
import com.cts.agrilink.inputAndProcurementMangement.dto.UpdateStockDTO;
import com.cts.agrilink.inputAndProcurementMangement.exception.GlobalExceptionHandler;
import com.cts.agrilink.inputAndProcurementMangement.exception.ResourceNotFoundException;
import com.cts.agrilink.inputAndProcurementMangement.model.AgriInput;
import com.cts.agrilink.inputAndProcurementMangement.service.AgriInputService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AgriInputControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AgriInputService agriInputService;

    @InjectMocks
    private AgriInputController agriInputController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AgriInput sampleInput;

    private static final String BASE_URL = "/agriLink/procurementManagement/inputs";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(agriInputController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleInput = AgriInput.builder()
                .inputId(1L).name("Urea").category("Fertiliser")
                .unit("Kg").pricePerUnit(20.0).subsidisedPrice(15.0)
                .availableStock(500).status("Available")
                .build();
    }

    // ─── GET /inputs ─────────────────────────────────────────────────────────────

    @Test
    void getAllInputs_returns200WithList() throws Exception {
        when(agriInputService.getAllInputs()).thenReturn(List.of(sampleInput));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Urea"))
                .andExpect(jsonPath("$[0].category").value("Fertiliser"));
    }

    // ─── GET /inputs/{inputId} ───────────────────────────────────────────────────

    @Test
    void getInputById_existingId_returns200() throws Exception {
        when(agriInputService.getInputById(1L)).thenReturn(sampleInput);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inputId").value(1))
                .andExpect(jsonPath("$.name").value("Urea"));
    }

    @Test
    void getInputById_nonExistingId_returns404() throws Exception {
        when(agriInputService.getInputById(99L))
                .thenThrow(new ResourceNotFoundException("Input not found with ID: 99"));

        mockMvc.perform(get(BASE_URL + "/99"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /inputs/category/{category} ────────────────────────────────────────

    @Test
    void getInputsByCategory_validCategory_returns200() throws Exception {
        when(agriInputService.getInputsByCategory("Fertiliser")).thenReturn(List.of(sampleInput));

        mockMvc.perform(get(BASE_URL + "/category/Fertiliser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Fertiliser"));
    }

    @Test
    void getInputsByCategory_invalidCategory_returns400() throws Exception {
        when(agriInputService.getInputsByCategory("Chemical"))
                .thenThrow(new IllegalArgumentException("Invalid category: Chemical"));

        mockMvc.perform(get(BASE_URL + "/category/Chemical"))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /inputs/status/{status} ─────────────────────────────────────────────

    @Test
    void getInputsByStatus_validStatus_returns200() throws Exception {
        when(agriInputService.getInputsByStatus("Available")).thenReturn(List.of(sampleInput));

        mockMvc.perform(get(BASE_URL + "/status/Available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("Available"));
    }

    @Test
    void getInputsByStatus_invalidStatus_returns400() throws Exception {
        when(agriInputService.getInputsByStatus("Expired"))
                .thenThrow(new IllegalArgumentException("Invalid status: Expired"));

        mockMvc.perform(get(BASE_URL + "/status/Expired"))
                .andExpect(status().isBadRequest());
    }

    // ─── POST /inputs ─────────────────────────────────────────────────────────────

    @Test
    void addInput_validRequest_returns201WithMessage() throws Exception {
        AgriInputRequestDTO dto = new AgriInputRequestDTO(
                "Urea", "Fertiliser", "Kg", 20.0, 15.0, 500, "Available");
        when(agriInputService.addInput(any())).thenReturn(new MessageResponseDTO("Input catalog item created successfully"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Input catalog item created successfully"));
    }

    @Test
    void addInput_missingName_returns400() throws Exception {
        AgriInputRequestDTO dto = new AgriInputRequestDTO(
                "", "Fertiliser", "Kg", 20.0, 15.0, 500, "Available");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ─── PUT /inputs/{inputId} ───────────────────────────────────────────────────

    @Test
    void updateInput_existingId_returns200WithMessage() throws Exception {
        UpdateInputDTO dto = new UpdateInputDTO("Urea Plus", 25.0, 18.0, 600, "Available");
        when(agriInputService.updateInput(eq(1L), any()))
                .thenReturn(new MessageResponseDTO("Input catalog item updated successfully"));

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Input catalog item updated successfully"));
    }

    @Test
    void updateInput_nonExistingId_returns404() throws Exception {
        UpdateInputDTO dto = new UpdateInputDTO("X", 1.0, 0.5, 10, "Available");
        when(agriInputService.updateInput(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Input not found with ID: 99"));

        mockMvc.perform(put(BASE_URL + "/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /inputs/{inputId}/stock ─────────────────────────────────────────────

    @Test
    void updateStock_validRequest_returns200() throws Exception {
        UpdateStockDTO dto = new UpdateStockDTO(200);
        when(agriInputService.updateStock(eq(1L), any()))
                .thenReturn(new MessageResponseDTO("Stock updated successfully"));

        mockMvc.perform(put(BASE_URL + "/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Stock updated successfully"));
    }

    // ─── DELETE /inputs/{inputId} ────────────────────────────────────────────────

    @Test
    void deleteInput_existingId_returns200WithMessage() throws Exception {
        when(agriInputService.deleteInput(1L))
                .thenReturn(new MessageResponseDTO("Input catalog item deleted successfully"));

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Input catalog item deleted successfully"));
    }

    @Test
    void deleteInput_nonExistingId_returns404() throws Exception {
        when(agriInputService.deleteInput(99L))
                .thenThrow(new ResourceNotFoundException("Input not found with ID: 99"));

        mockMvc.perform(delete(BASE_URL + "/99"))
                .andExpect(status().isNotFound());
    }
}
