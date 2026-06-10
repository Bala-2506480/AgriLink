package com.cts.agrilink.inputAndProcurementMangement.service;

import com.cts.agrilink.inputAndProcurementMangement.dto.AgriInputRequestDTO;
import com.cts.agrilink.inputAndProcurementMangement.dto.MessageResponseDTO;
import com.cts.agrilink.inputAndProcurementMangement.dto.UpdateInputDTO;
import com.cts.agrilink.inputAndProcurementMangement.dto.UpdateStockDTO;
import com.cts.agrilink.inputAndProcurementMangement.exception.ResourceNotFoundException;
import com.cts.agrilink.inputAndProcurementMangement.model.AgriInput;
import com.cts.agrilink.inputAndProcurementMangement.repository.AgriInputRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgriInputService {

    private final AgriInputRepository agriInputRepository;

    public List<AgriInput> getAllInputs() {
        return agriInputRepository.findAll();
    }

    public AgriInput getInputById(Long inputId) {
        return agriInputRepository.findById(inputId)
                .orElseThrow(() -> new ResourceNotFoundException("Input not found with ID: " + inputId));
    }

    public List<AgriInput> getInputsByCategory(String category) {
        List<String> validCategories = List.of("Seed", "Fertiliser", "Pesticide", "Equipment");
        if (!validCategories.contains(category)) {
            throw new IllegalArgumentException("Invalid category: " + category);
        }
        return agriInputRepository.findByCategory(category);
    }

    public List<AgriInput> getInputsByStatus(String status) {
        List<String> validStatuses = List.of("Available", "OutOfStock");
        if (!validStatuses.contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        return agriInputRepository.findByStatus(status);
    }

    public MessageResponseDTO addInput(AgriInputRequestDTO dto) {
        AgriInput input = AgriInput.builder()
                .name(dto.getName())
                .category(dto.getCategory())
                .unit(dto.getUnit())
                .pricePerUnit(dto.getPricePerUnit())
                .subsidisedPrice(dto.getSubsidisedPrice())
                .availableStock(dto.getAvailableStock())
                .status(dto.getStatus())
                .build();

        agriInputRepository.save(input);
        return new MessageResponseDTO("Input catalog item created successfully");
    }

    public MessageResponseDTO updateInput(Long inputId, UpdateInputDTO dto) {
        AgriInput input = agriInputRepository.findById(inputId)
                .orElseThrow(() -> new ResourceNotFoundException("Input not found with ID: " + inputId));

        input.setName(dto.getName());
        input.setPricePerUnit(dto.getPricePerUnit());
        input.setSubsidisedPrice(dto.getSubsidisedPrice());
        input.setAvailableStock(dto.getAvailableStock());
        input.setStatus(dto.getStatus());

        agriInputRepository.save(input);
        return new MessageResponseDTO("Input catalog item updated successfully");
    }

    public MessageResponseDTO updateStock(Long inputId, UpdateStockDTO dto) {
        AgriInput input = agriInputRepository.findById(inputId)
                .orElseThrow(() -> new ResourceNotFoundException("Input not found with ID: " + inputId));

        input.setAvailableStock(dto.getAvailableStock());
        agriInputRepository.save(input);
        return new MessageResponseDTO("Stock updated successfully");
    }

    public MessageResponseDTO deleteInput(Long inputId) {
        if (!agriInputRepository.existsById(inputId)) {
            throw new ResourceNotFoundException("Input not found with ID: " + inputId);
        }
        agriInputRepository.deleteById(inputId);
        return new MessageResponseDTO("Input catalog item deleted successfully");
    }
}
