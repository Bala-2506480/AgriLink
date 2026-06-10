package com.cts.agrilink.inputAndProcurementMangement.controller;

import com.cts.agrilink.inputAndProcurementMangement.dto.AgriInputRequestDTO;
import com.cts.agrilink.inputAndProcurementMangement.dto.MessageResponseDTO;
import com.cts.agrilink.inputAndProcurementMangement.dto.UpdateInputDTO;
import com.cts.agrilink.inputAndProcurementMangement.dto.UpdateStockDTO;
import com.cts.agrilink.inputAndProcurementMangement.model.AgriInput;
import com.cts.agrilink.inputAndProcurementMangement.service.AgriInputService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agriLink/procurementManagement/inputs")
@RequiredArgsConstructor
public class AgriInputController {

    private final AgriInputService agriInputService;

    // GET - Section 1, Endpoint #1
    @GetMapping
    public ResponseEntity<List<AgriInput>> getAllInputs() {
        return ResponseEntity.ok(agriInputService.getAllInputs());
    }

    // GET - Section 1, Endpoint #2
    @GetMapping("/{inputId}")
    public ResponseEntity<AgriInput> getInputById(@PathVariable Long inputId) {
        return ResponseEntity.ok(agriInputService.getInputById(inputId));
    }

    // GET - Section 1, Endpoint #3
    @GetMapping("/category/{category}")
    public ResponseEntity<List<AgriInput>> getInputsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(agriInputService.getInputsByCategory(category));
    }

    // GET - Section 1, Endpoint #4
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AgriInput>> getInputsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(agriInputService.getInputsByStatus(status));
    }

    // POST - Section 1, Endpoint #5
    @PostMapping
    public ResponseEntity<MessageResponseDTO> addInput(@Valid @RequestBody AgriInputRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agriInputService.addInput(dto));
    }

    // PUT - Section 1, Endpoint #6
    @PutMapping("/{inputId}")
    public ResponseEntity<MessageResponseDTO> updateInput(@PathVariable Long inputId,
                                                          @Valid @RequestBody UpdateInputDTO dto) {
        return ResponseEntity.ok(agriInputService.updateInput(inputId, dto));
    }

    // PUT - Section 1, Endpoint #7
    @PutMapping("/{inputId}/stock")
    public ResponseEntity<MessageResponseDTO> updateStock(@PathVariable Long inputId,
                                                          @Valid @RequestBody UpdateStockDTO dto) {
        return ResponseEntity.ok(agriInputService.updateStock(inputId, dto));
    }

    // DELETE - Section 1, Endpoint #8
    @DeleteMapping("/{inputId}")
    public ResponseEntity<MessageResponseDTO> deleteInput(@PathVariable Long inputId) {
        return ResponseEntity.ok(agriInputService.deleteInput(inputId));
    }
}
