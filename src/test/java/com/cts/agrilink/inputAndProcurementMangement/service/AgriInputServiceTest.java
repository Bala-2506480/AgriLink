package com.cts.agrilink.inputAndProcurementMangement.service;

import com.cts.agrilink.inputAndProcurementMangement.dto.AgriInputRequestDTO;
import com.cts.agrilink.inputAndProcurementMangement.dto.MessageResponseDTO;
import com.cts.agrilink.inputAndProcurementMangement.dto.UpdateInputDTO;
import com.cts.agrilink.inputAndProcurementMangement.dto.UpdateStockDTO;
import com.cts.agrilink.inputAndProcurementMangement.exception.ResourceNotFoundException;
import com.cts.agrilink.inputAndProcurementMangement.model.AgriInput;
import com.cts.agrilink.inputAndProcurementMangement.repository.AgriInputRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgriInputServiceTest {

    @Mock
    private AgriInputRepository agriInputRepository;

    @InjectMocks
    private AgriInputService agriInputService;

    private AgriInput sampleInput;

    @BeforeEach
    void setUp() {
        sampleInput = AgriInput.builder()
                .inputId(1L)
                .name("Urea")
                .category("Fertiliser")
                .unit("Kg")
                .pricePerUnit(20.0)
                .subsidisedPrice(15.0)
                .availableStock(500)
                .status("Available")
                .build();
    }

    // ─── getAllInputs ────────────────────────────────────────────────────────────

    @Test
    void getAllInputs_returnsListFromRepository() {
        when(agriInputRepository.findAll()).thenReturn(List.of(sampleInput));

        List<AgriInput> result = agriInputService.getAllInputs();

        assertThat(result).hasSize(1).contains(sampleInput);
    }

    // ─── getInputById ────────────────────────────────────────────────────────────

    @Test
    void getInputById_existingId_returnsInput() {
        when(agriInputRepository.findById(1L)).thenReturn(Optional.of(sampleInput));

        AgriInput result = agriInputService.getInputById(1L);

        assertThat(result).isEqualTo(sampleInput);
    }

    @Test
    void getInputById_nonExistingId_throwsResourceNotFoundException() {
        when(agriInputRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agriInputService.getInputById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ─── getInputsByCategory ─────────────────────────────────────────────────────

    @Test
    void getInputsByCategory_validCategory_returnsFilteredList() {
        when(agriInputRepository.findByCategoryIgnoreCase("Seed")).thenReturn(List.of(sampleInput));

        List<AgriInput> result = agriInputService.getInputsByCategory("Seed");

        assertThat(result).hasSize(1);
    }

    @Test
    void getInputsByCategory_lowercaseCategory_returnsFilteredList() {
        when(agriInputRepository.findByCategoryIgnoreCase("fertiliser")).thenReturn(List.of(sampleInput));

        List<AgriInput> result = agriInputService.getInputsByCategory("fertiliser");

        assertThat(result).hasSize(1);
    }

    @Test
    void getInputsByCategory_invalidCategory_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> agriInputService.getInputsByCategory("Chemical"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid category");
    }

    // ─── getInputsByStatus ───────────────────────────────────────────────────────

    @Test
    void getInputsByStatus_validStatus_returnsFilteredList() {
        when(agriInputRepository.findByStatusIgnoreCase("Available")).thenReturn(List.of(sampleInput));

        List<AgriInput> result = agriInputService.getInputsByStatus("Available");

        assertThat(result).hasSize(1);
    }

    @Test
    void getInputsByStatus_invalidStatus_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> agriInputService.getInputsByStatus("Expired"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status");
    }

    // ─── addInput ────────────────────────────────────────────────────────────────

    @Test
    void addInput_validDto_savesAndReturnsSuccessMessage() {
        AgriInputRequestDTO dto = new AgriInputRequestDTO(
                "Urea", "Fertiliser", "Kg", 20.0, 15.0, 500, "Available");

        MessageResponseDTO response = agriInputService.addInput(dto);

        verify(agriInputRepository).save(any(AgriInput.class));
        assertThat(response.getMessage()).contains("created successfully");
    }

    // ─── updateInput ─────────────────────────────────────────────────────────────

    @Test
    void updateInput_existingId_updatesFieldsAndReturnsSuccessMessage() {
        when(agriInputRepository.findById(1L)).thenReturn(Optional.of(sampleInput));
        UpdateInputDTO dto = new UpdateInputDTO("Urea Plus", 25.0, 18.0, 600, "Available");

        MessageResponseDTO response = agriInputService.updateInput(1L, dto);

        verify(agriInputRepository).save(sampleInput);
        assertThat(response.getMessage()).contains("updated successfully");
        assertThat(sampleInput.getName()).isEqualTo("Urea Plus");
        assertThat(sampleInput.getPricePerUnit()).isEqualTo(25.0);
    }

    @Test
    void updateInput_nonExistingId_throwsResourceNotFoundException() {
        when(agriInputRepository.findById(99L)).thenReturn(Optional.empty());
        UpdateInputDTO dto = new UpdateInputDTO("X", 1.0, 0.5, 10, "Available");

        assertThatThrownBy(() -> agriInputService.updateInput(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── updateStock ─────────────────────────────────────────────────────────────

    @Test
    void updateStock_existingId_updatesStockAndReturnsSuccessMessage() {
        when(agriInputRepository.findById(1L)).thenReturn(Optional.of(sampleInput));
        UpdateStockDTO dto = new UpdateStockDTO(200);

        MessageResponseDTO response = agriInputService.updateStock(1L, dto);

        assertThat(sampleInput.getAvailableStock()).isEqualTo(200);
        assertThat(response.getMessage()).contains("Stock updated");
    }

    @Test
    void updateStock_nonExistingId_throwsResourceNotFoundException() {
        when(agriInputRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agriInputService.updateStock(99L, new UpdateStockDTO(10)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── deleteInput ─────────────────────────────────────────────────────────────

    @Test
    void deleteInput_existingId_deletesAndReturnsSuccessMessage() {
        when(agriInputRepository.existsById(1L)).thenReturn(true);

        MessageResponseDTO response = agriInputService.deleteInput(1L);

        verify(agriInputRepository).deleteById(1L);
        assertThat(response.getMessage()).contains("deleted successfully");
    }

    @Test
    void deleteInput_nonExistingId_throwsResourceNotFoundException() {
        when(agriInputRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> agriInputService.deleteInput(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
