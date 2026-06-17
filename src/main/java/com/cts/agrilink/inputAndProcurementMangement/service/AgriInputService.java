package com.cts.agrilink.inputAndProcurementMangement.service;

import com.cts.agrilink.inputAndProcurementMangement.dto.AgriInputRequestDTO;
import com.cts.agrilink.inputAndProcurementMangement.dto.MessageResponseDTO;
import com.cts.agrilink.inputAndProcurementMangement.dto.UpdateInputDTO;
import com.cts.agrilink.inputAndProcurementMangement.dto.UpdateStockDTO;
import com.cts.agrilink.inputAndProcurementMangement.exception.ResourceNotFoundException;
import com.cts.agrilink.inputAndProcurementMangement.model.AgriInput;
import com.cts.agrilink.inputAndProcurementMangement.repository.AgriInputRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AgriInputService {

    private final AgriInputRepository agriInputRepository;

    public List<AgriInput> getAllInputs() {
        log.debug("Fetching all agri inputs");
        return agriInputRepository.findAll();
    }

    public AgriInput getInputById(Long inputId) {
        log.debug("Fetching agri input with ID: {}", inputId);
        return agriInputRepository.findById(inputId)
                .orElseThrow(() -> new ResourceNotFoundException("Input not found with ID: " + inputId));
    }

    public List<AgriInput> getInputsByCategory(String category) {
        log.debug("Fetching agri inputs in category: {}", category);
        List<String> validCategories = List.of("Seed", "Fertiliser", "Pesticide", "Equipment");
        if (validCategories.stream().noneMatch(c -> c.equalsIgnoreCase(category))) {
            log.warn("Rejected request: invalid category '{}'", category);
            throw new IllegalArgumentException("Invalid category: " + category);
        }
        return agriInputRepository.findByCategoryIgnoreCase(category);
    }

    public List<AgriInput> getInputsByStatus(String status) {
        log.debug("Fetching agri inputs with status: {}", status);
        List<String> validStatuses = List.of("Available", "OutOfStock");
        if (validStatuses.stream().noneMatch(s -> s.equalsIgnoreCase(status))) {
            log.warn("Rejected request: invalid status '{}'", status);
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        return agriInputRepository.findByStatusIgnoreCase(status);
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
        log.info("Added agri input to catalog: name='{}', category='{}'", dto.getName(), dto.getCategory());
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
        log.info("Updated agri input ID: {}", inputId);
        return new MessageResponseDTO("Input catalog item updated successfully");
    }

    public MessageResponseDTO updateStock(Long inputId, UpdateStockDTO dto) {
        AgriInput input = agriInputRepository.findById(inputId)
                .orElseThrow(() -> new ResourceNotFoundException("Input not found with ID: " + inputId));

        input.setAvailableStock(dto.getAvailableStock());
        agriInputRepository.save(input);
        log.info("Updated stock for agri input ID: {} to {}", inputId, dto.getAvailableStock());
        return new MessageResponseDTO("Stock updated successfully");
    }

    public MessageResponseDTO deleteInput(Long inputId) {
        if (!agriInputRepository.existsById(inputId)) {
            throw new ResourceNotFoundException("Input not found with ID: " + inputId);
        }
        agriInputRepository.deleteById(inputId);
        log.info("Deleted agri input ID: {}", inputId);
        return new MessageResponseDTO("Input catalog item deleted successfully");
    }
}
