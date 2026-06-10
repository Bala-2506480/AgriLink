package com.cts.agrilink.inputAndProcurementMangement.service;

import com.cts.agrilink.inputAndProcurementMangement.dto.*;
import com.cts.agrilink.inputAndProcurementMangement.exception.ResourceNotFoundException;
import com.cts.agrilink.inputAndProcurementMangement.exception.StateConflictException;
import com.cts.agrilink.inputAndProcurementMangement.model.AgriInput;
import com.cts.agrilink.inputAndProcurementMangement.model.InputRequest;
import com.cts.agrilink.inputAndProcurementMangement.repository.AgriInputRepository;
import com.cts.agrilink.inputAndProcurementMangement.repository.InputRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InputRequestService {

    private final InputRequestRepository inputRequestRepository;
    private final AgriInputRepository agriInputRepository;

    public List<InputRequest> getAllRequests() {
        return inputRequestRepository.findAll();
    }

    public InputRequest getRequestById(Long requestId) {
        return inputRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Input request not found with ID: " + requestId));
    }

    public List<InputRequest> getRequestsByFarmer(Long farmerId) {
        return inputRequestRepository.findByFarmerId(farmerId);
    }

    public List<InputRequest> getRequestsByStatus(String status) {
        List<String> validStatuses = List.of("Requested", "Approved", "Dispatched", "Delivered", "Cancelled");
        if (!validStatuses.contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        return inputRequestRepository.findByStatus(status);
    }

    public List<InputRequest> getRequestsByCentre(Long centreId) {
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
        return new MessageResponseDTO("Input request updated successfully");
    }

    public MessageResponseDTO approveRequest(Long requestId, ApproveRequestDTO dto) {
        InputRequest request = inputRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Input request not found with ID: " + requestId));

        if (!"Requested".equals(request.getStatus())) {
            throw new StateConflictException("Request cannot be approved. Current status: " + request.getStatus());
        }

        request.setStatus("Approved");
        request.setAssignedCentreId(dto.getAssignedCentreId());
        request.setActualPrice(dto.getActualPrice());
        request.setApprovedBy(dto.getApprovedBy());

        inputRequestRepository.save(request);
        return new MessageResponseDTO("Input request approved successfully");
    }

    public MessageResponseDTO dispatchRequest(Long requestId, DispatchRequestDTO dto) {
        InputRequest request = inputRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Input request not found with ID: " + requestId));

        if (!"Approved".equals(request.getStatus())) {
            throw new StateConflictException("Request cannot be dispatched. Current status: " + request.getStatus());
        }

        request.setStatus("Dispatched");
        request.setDispatchedBy(dto.getDispatchedBy());
        request.setDispatchDate(dto.getDispatchDate());

        inputRequestRepository.save(request);
        return new MessageResponseDTO("Dispatched successfully");
    }

    public MessageResponseDTO deliverRequest(Long requestId, DeliverRequestDTO dto) {
        InputRequest request = inputRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Input request not found with ID: " + requestId));

        if (!"Dispatched".equals(request.getStatus())) {
            throw new StateConflictException("Request cannot be delivered. Current status: " + request.getStatus());
        }

        request.setStatus("Delivered");
        request.setDeliveredDate(dto.getDeliveredDate());
        request.setReceivedBy(dto.getReceivedBy());

        inputRequestRepository.save(request);
        return new MessageResponseDTO("Delivered to farmer successfully");
    }

    public MessageResponseDTO cancelRequest(Long requestId, CancelRequestDTO dto) {
        InputRequest request = inputRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Input request not found with ID: " + requestId));

        if ("Dispatched".equals(request.getStatus()) || "Delivered".equals(request.getStatus())) {
            throw new StateConflictException("Cannot cancel request after dispatch. Current status: " + request.getStatus());
        }

        request.setStatus("Cancelled");
        request.setReason(dto.getReason());

        inputRequestRepository.save(request);
        return new MessageResponseDTO("Request cancelled successfully");
    }

    public MessageResponseDTO deleteRequest(Long requestId) {
        if (!inputRequestRepository.existsById(requestId)) {
            throw new ResourceNotFoundException("Input request not found with ID: " + requestId);
        }
        inputRequestRepository.deleteById(requestId);
        return new MessageResponseDTO("Input request record deleted successfully");
    }
}
