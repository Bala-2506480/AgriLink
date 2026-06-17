package com.cts.agrilink.inputAndProcurementMangement.controller;

import com.cts.agrilink.inputAndProcurementMangement.dto.*;
import com.cts.agrilink.inputAndProcurementMangement.model.InputRequest;
import com.cts.agrilink.inputAndProcurementMangement.service.InputRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agriLink/procurementManagement/input-requests")
@Slf4j
@RequiredArgsConstructor
public class InputRequestController {

    private final InputRequestService inputRequestService;

    @GetMapping
    public ResponseEntity<List<InputRequest>> getAllRequests() {
        return ResponseEntity.ok(inputRequestService.getAllRequests());
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<InputRequest> getRequestById(@PathVariable Long requestId) {
        return ResponseEntity.ok(inputRequestService.getRequestById(requestId));
    }

    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<InputRequest>> getRequestsByFarmer(@PathVariable Long farmerId) {
        return ResponseEntity.ok(inputRequestService.getRequestsByFarmer(farmerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<InputRequest>> getRequestsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(inputRequestService.getRequestsByStatus(status));
    }

    @GetMapping("/centre/{centreId}")
    public ResponseEntity<List<InputRequest>> getRequestsByCentre(@PathVariable Long centreId) {
        return ResponseEntity.ok(inputRequestService.getRequestsByCentre(centreId));
    }

    @PostMapping
    public ResponseEntity<MessageResponseDTO> submitRequest(@Valid @RequestBody InputProcurementRequestDTO dto) {
        log.info("POST /input-requests - submit request for farmerId={}", dto.getFarmerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(inputRequestService.submitRequest(dto));
    }

    @PutMapping("/{requestId}")
    public ResponseEntity<MessageResponseDTO> updateRequest(@PathVariable Long requestId,
                                                             @Valid @RequestBody UpdateInputRequestDTO dto) {
        log.info("PUT /input-requests/{} - update request", requestId);
        return ResponseEntity.ok(inputRequestService.updateRequest(requestId, dto));
    }

    @PutMapping("/{requestId}/approve")
    public ResponseEntity<MessageResponseDTO> approveRequest(@PathVariable Long requestId,
                                                              @Valid @RequestBody ApproveRequestDTO dto) {
        log.info("PUT /input-requests/{}/approve", requestId);
        return ResponseEntity.ok(inputRequestService.approveRequest(requestId, dto));
    }

    @PutMapping("/{requestId}/dispatch")
    public ResponseEntity<MessageResponseDTO> dispatchRequest(@PathVariable Long requestId,
                                                              @Valid @RequestBody DispatchRequestDTO dto) {
        log.info("PUT /input-requests/{}/dispatch", requestId);
        return ResponseEntity.ok(inputRequestService.dispatchRequest(requestId, dto));
    }

    @PutMapping("/{requestId}/deliver")
    public ResponseEntity<MessageResponseDTO> deliverRequest(@PathVariable Long requestId,
                                                             @Valid @RequestBody DeliverRequestDTO dto) {
        log.info("PUT /input-requests/{}/deliver", requestId);
        return ResponseEntity.ok(inputRequestService.deliverRequest(requestId, dto));
    }

    @PutMapping("/{requestId}/cancel")
    public ResponseEntity<MessageResponseDTO> cancelRequest(@PathVariable Long requestId,
                                                            @Valid @RequestBody CancelRequestDTO dto) {
        log.info("PUT /input-requests/{}/cancel", requestId);
        return ResponseEntity.ok(inputRequestService.cancelRequest(requestId, dto));
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<MessageResponseDTO> deleteRequest(@PathVariable Long requestId) {
        log.info("DELETE /input-requests/{}", requestId);
        return ResponseEntity.ok(inputRequestService.deleteRequest(requestId));
    }
}
