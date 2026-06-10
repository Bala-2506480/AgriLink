package com.cts.agrilink.inputAndProcurementMangement.controller;

import com.cts.agrilink.inputAndProcurementMangement.dto.*;
import com.cts.agrilink.inputAndProcurementMangement.model.InputRequest;
import com.cts.agrilink.inputAndProcurementMangement.service.InputRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agriLink/procurementManagement/input-requests")
@RequiredArgsConstructor
public class InputRequestController {

    private final InputRequestService inputRequestService;

    // GET - Section 2, Endpoint #1
    @GetMapping
    public ResponseEntity<List<InputRequest>> getAllRequests() {
        return ResponseEntity.ok(inputRequestService.getAllRequests());
    }

    // GET - Section 2, Endpoint #2
    @GetMapping("/{requestId}")
    public ResponseEntity<InputRequest> getRequestById(@PathVariable Long requestId) {
        return ResponseEntity.ok(inputRequestService.getRequestById(requestId));
    }

    // GET - Section 2, Endpoint #3
    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<InputRequest>> getRequestsByFarmer(@PathVariable Long farmerId) {
        return ResponseEntity.ok(inputRequestService.getRequestsByFarmer(farmerId));
    }

    // GET - Section 2, Endpoint #4
    @GetMapping("/status/{status}")
    public ResponseEntity<List<InputRequest>> getRequestsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(inputRequestService.getRequestsByStatus(status));
    }

    // GET - Section 2, Endpoint #5
    @GetMapping("/centre/{centreId}")
    public ResponseEntity<List<InputRequest>> getRequestsByCentre(@PathVariable Long centreId) {
        return ResponseEntity.ok(inputRequestService.getRequestsByCentre(centreId));
    }

    // POST - Section 2, Endpoint #6
    @PostMapping
    public ResponseEntity<MessageResponseDTO> submitRequest(@Valid @RequestBody InputProcurementRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inputRequestService.submitRequest(dto));
    }

    // PUT - Section 2, Endpoint #7
    @PutMapping("/{requestId}")
    public ResponseEntity<MessageResponseDTO> updateRequest(@PathVariable Long requestId,
                                                             @Valid @RequestBody UpdateInputRequestDTO dto) {
        return ResponseEntity.ok(inputRequestService.updateRequest(requestId, dto));
    }

    // PUT - Section 2, Endpoint #8
    @PutMapping("/{requestId}/approve")
    public ResponseEntity<MessageResponseDTO> approveRequest(@PathVariable Long requestId,
                                                              @Valid @RequestBody ApproveRequestDTO dto) {
        return ResponseEntity.ok(inputRequestService.approveRequest(requestId, dto));
    }

    // PUT - Section 2, Endpoint #9
    @PutMapping("/{requestId}/dispatch")
    public ResponseEntity<MessageResponseDTO> dispatchRequest(@PathVariable Long requestId,
                                                               @Valid @RequestBody DispatchRequestDTO dto) {
        return ResponseEntity.ok(inputRequestService.dispatchRequest(requestId, dto));
    }

    // PUT - Section 2, Endpoint #10
    @PutMapping("/{requestId}/deliver")
    public ResponseEntity<MessageResponseDTO> deliverRequest(@PathVariable Long requestId,
                                                              @Valid @RequestBody DeliverRequestDTO dto) {
        return ResponseEntity.ok(inputRequestService.deliverRequest(requestId, dto));
    }

    // PUT - Section 2, Endpoint #11
    @PutMapping("/{requestId}/cancel")
    public ResponseEntity<MessageResponseDTO> cancelRequest(@PathVariable Long requestId,
                                                             @Valid @RequestBody CancelRequestDTO dto) {
        return ResponseEntity.ok(inputRequestService.cancelRequest(requestId, dto));
    }

    // DELETE - Section 2, Endpoint #12
    @DeleteMapping("/{requestId}")
    public ResponseEntity<MessageResponseDTO> deleteRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(inputRequestService.deleteRequest(requestId));
    }
}
