package com.cts.agrilink.inputAndProcurementMangement.service;

import com.cts.agrilink.inputAndProcurementMangement.dto.*;
import com.cts.agrilink.inputAndProcurementMangement.exception.ResourceNotFoundException;
import com.cts.agrilink.inputAndProcurementMangement.exception.StateConflictException;
import com.cts.agrilink.inputAndProcurementMangement.model.AgriInput;
import com.cts.agrilink.inputAndProcurementMangement.model.InputRequest;
import com.cts.agrilink.inputAndProcurementMangement.repository.AgriInputRepository;
import com.cts.agrilink.inputAndProcurementMangement.repository.InputRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InputRequestService {

    private final InputRequestRepository inputRequestRepository;
    private final AgriInputRepository agriInputRepository;

    public List<InputRequest> getAllRequests() {
        log.debug("Fetching all input requests");
        return inputRequestRepository.findAll();
    }

    public InputRequest getRequestById(Long requestId) {
        log.debug("Fetching input request with ID: {}", requestId);
        return inputRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Input request not found with ID: " + requestId));
    }

    public List<InputRequest> getRequestsByFarmer(Long farmerId) {
        log.debug("Fetching input requests for farmer ID: {}", farmerId);
        return inputRequestRepository.findByFarmerId(farmerId);
    }

    public List<InputRequest> getRequestsByStatus(String status) {
        log.debug("Fetching input requests with status: {}", status);
        List<String> validStatuses = List.of("Requested", "Approved", "Dispatched", "Delivered", "Cancelled");
        if (validStatuses.stream().noneMatch(s -> s.equalsIgnoreCase(status))) {
            log.warn("Rejected request: invalid status '{}'", status);
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        return inputRequestRepository.findByStatusIgnoreCase(status);
    }

    public List<InputRequest> getRequestsByCentre(Long centreId) {
        log.debug("Fetching input requests for centre ID: {}", centreId);
        return inputRequestRepository.findByAssignedCentreId(centreId);
    }

    public MessageResponseDTO submitRequest(InputProcurementRequestDTO dto) {
        AgriInput input = agriInputRepository.findById(dto.getInputId())
                .orElseThrow(() -> new ResourceNotFoundException("Input not found with ID: " + dto.getInputId()));

        InputRequest request = InputRequest.builder()
                .farmerId(dto.getFarmerId())
                .inputId(dto.getInputId())
                .quantityRequested(dto.getQuantityRequested())
                .requestDate(dto.getRequestDate())
                .assignedCentreId(dto.getAssignedCentreId())
                .actualPrice(dto.getActualPrice())
                .status("Requested")
                .build();

        inputRequestRepository.save(request);
        log.info("Submitted input request: farmerId={}, inputId={}, qty={}",
                dto.getFarmerId(), dto.getInputId(), dto.getQuantityRequested());
        return new MessageResponseDTO("Input request submitted successfully");
    }

    public MessageResponseDTO updateRequest(Long requestId, UpdateInputRequestDTO dto) {
        InputRequest request = inputRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Input request not found with ID: " + requestId));

        if (dto.getQuantityRequested() != null) {
            request.setQuantityRequested(dto.getQuantityRequested());
        }
        if (dto.getAssignedCentreId() != null) {
            request.setAssignedCentreId(dto.getAssignedCentreId());
        }
        if (dto.getActualPrice() != null) {
            request.setActualPrice(dto.getActualPrice());
        }

        inputRequestRepository.save(request);
        log.info("Updated input request ID: {}", requestId);
        return new MessageResponseDTO("Input request updated successfully");
    }

    public MessageResponseDTO approveRequest(Long requestId, ApproveRequestDTO dto) {
        InputRequest request = inputRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Input request not found with ID: " + requestId));

        if (!"Requested".equals(request.getStatus())) {
            log.warn("Approve rejected for request ID {}: current status is '{}'", requestId, request.getStatus());
            throw new StateConflictException("Request cannot be approved. Current status: " + request.getStatus());
        }

        request.setStatus("Approved");
        request.setAssignedCentreId(dto.getAssignedCentreId());
        request.setActualPrice(dto.getActualPrice());
        request.setApprovedBy(dto.getApprovedBy());

        inputRequestRepository.save(request);
        log.info("Approved input request ID: {} by {}", requestId, dto.getApprovedBy());
        return new MessageResponseDTO("Input request approved successfully");
    }

    public MessageResponseDTO dispatchRequest(Long requestId, DispatchRequestDTO dto) {
        InputRequest request = inputRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Input request not found with ID: " + requestId));

        if (!"Approved".equals(request.getStatus())) {
            log.warn("Dispatch rejected for request ID {}: current status is '{}'", requestId, request.getStatus());
            throw new StateConflictException("Request cannot be dispatched. Current status: " + request.getStatus());
        }

        request.setStatus("Dispatched");
        request.setDispatchedBy(dto.getDispatchedBy());
        request.setDispatchDate(dto.getDispatchDate());
        inputRequestRepository.save(request);
        log.info("Dispatched input request ID: {} by {}", requestId, dto.getDispatchedBy());
        return new MessageResponseDTO("Dispatched successfully");
    }

    public MessageResponseDTO deliverRequest(Long requestId, DeliverRequestDTO dto) {
        InputRequest request = inputRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Input request not found with ID: " + requestId));

        if (!"Dispatched".equals(request.getStatus())) {
            log.warn("Deliver rejected for request ID {}: current status is '{}'", requestId, request.getStatus());
            throw new StateConflictException("Request cannot be delivered. Current status: " + request.getStatus());
        }

        request.setStatus("Delivered");
        request.setDeliveredDate(dto.getDeliveredDate());
        request.setReceivedBy(dto.getReceivedBy());
        inputRequestRepository.save(request);
        log.info("Delivered input request ID: {} received by {}", requestId, dto.getReceivedBy());
        return new MessageResponseDTO("Delivered to farmer successfully");
    }

    public MessageResponseDTO cancelRequest(Long requestId, CancelRequestDTO dto) {
        InputRequest request = inputRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Input request not found with ID: " + requestId));

        if ("Dispatched".equals(request.getStatus()) || "Delivered".equals(request.getStatus())) {
            log.warn("Cancel rejected for request ID {}: current status is '{}'", requestId, request.getStatus());
            throw new StateConflictException("Cannot cancel request after dispatch. Current status: " + request.getStatus());
        }

        request.setStatus("Cancelled");
        request.setCancellationReason(dto.getReason());
        inputRequestRepository.save(request);
        log.info("Cancelled input request ID: {} (reason: {})", requestId, dto.getReason());
        return new MessageResponseDTO("Request cancelled successfully");
    }

    public MessageResponseDTO deleteRequest(Long requestId) {
        if (!inputRequestRepository.existsById(requestId)) {
            throw new ResourceNotFoundException("Input request not found with ID: " + requestId);
        }
        inputRequestRepository.deleteById(requestId);
        log.info("Deleted input request ID: {}", requestId);
        return new MessageResponseDTO("Input request record deleted successfully");
    }
}
