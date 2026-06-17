package com.cts.agrilink.inputAndProcurementMangement.service;

import com.cts.agrilink.inputAndProcurementMangement.dto.*;
import com.cts.agrilink.inputAndProcurementMangement.exception.ResourceNotFoundException;
import com.cts.agrilink.inputAndProcurementMangement.exception.StateConflictException;
import com.cts.agrilink.inputAndProcurementMangement.model.AgriInput;
import com.cts.agrilink.inputAndProcurementMangement.model.InputRequest;
import com.cts.agrilink.inputAndProcurementMangement.repository.AgriInputRepository;
import com.cts.agrilink.inputAndProcurementMangement.repository.InputRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InputRequestServiceTest {

    @Mock
    private InputRequestRepository inputRequestRepository;

    @Mock
    private AgriInputRepository agriInputRepository;

    @InjectMocks
    private InputRequestService inputRequestService;

    private InputRequest requestedRequest;
    private InputRequest approvedRequest;
    private InputRequest dispatchedRequest;
    private AgriInput sampleInput;

    @BeforeEach
    void setUp() {
        sampleInput = AgriInput.builder()
                .inputId(10L).name("Urea").category("Fertiliser")
                .unit("Kg").pricePerUnit(20.0).availableStock(500).status("Available")
                .build();

        requestedRequest = InputRequest.builder()
                .requestId(1L).farmerId(100L).inputId(10L)
                .quantityRequested(50).requestDate(LocalDate.now())
                .assignedCentreId(5L).actualPrice(1000.0).status("Requested")
                .build();

        approvedRequest = InputRequest.builder()
                .requestId(2L).farmerId(101L).inputId(10L)
                .quantityRequested(30).requestDate(LocalDate.now())
                .assignedCentreId(5L).actualPrice(600.0).status("Approved")
                .build();

        dispatchedRequest = InputRequest.builder()
                .requestId(3L).farmerId(102L).inputId(10L)
                .quantityRequested(20).requestDate(LocalDate.now())
                .status("Dispatched")
                .build();
    }

    // ─── getAllRequests ───────────────────────────────────────────────────────────

    @Test
    void getAllRequests_returnsListFromRepository() {
        when(inputRequestRepository.findAll()).thenReturn(List.of(requestedRequest));

        List<InputRequest> result = inputRequestService.getAllRequests();

        assertThat(result).hasSize(1);
    }

    // ─── getRequestById ───────────────────────────────────────────────────────────

    @Test
    void getRequestById_existingId_returnsRequest() {
        when(inputRequestRepository.findById(1L)).thenReturn(Optional.of(requestedRequest));

        InputRequest result = inputRequestService.getRequestById(1L);

        assertThat(result).isEqualTo(requestedRequest);
    }

    @Test
    void getRequestById_nonExistingId_throwsResourceNotFoundException() {
        when(inputRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inputRequestService.getRequestById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ─── getRequestsByFarmer ─────────────────────────────────────────────────────

    @Test
    void getRequestsByFarmer_returnsFarmerRequests() {
        when(inputRequestRepository.findByFarmerId(100L)).thenReturn(List.of(requestedRequest));

        List<InputRequest> result = inputRequestService.getRequestsByFarmer(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFarmerId()).isEqualTo(100L);
    }

    // ─── getRequestsByStatus ─────────────────────────────────────────────────────

    @Test
    void getRequestsByStatus_validStatus_returnsFilteredList() {
        when(inputRequestRepository.findByStatusIgnoreCase("Requested")).thenReturn(List.of(requestedRequest));

        List<InputRequest> result = inputRequestService.getRequestsByStatus("Requested");

        assertThat(result).hasSize(1);
    }

    @Test
    void getRequestsByStatus_invalidStatus_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> inputRequestService.getRequestsByStatus("Pending"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status");
    }

    // ─── getRequestsByCentre ─────────────────────────────────────────────────────

    @Test
    void getRequestsByCentre_returnsCentreRequests() {
        when(inputRequestRepository.findByAssignedCentreId(5L)).thenReturn(List.of(requestedRequest));

        List<InputRequest> result = inputRequestService.getRequestsByCentre(5L);

        assertThat(result).hasSize(1);
    }

    // ─── submitRequest ───────────────────────────────────────────────────────────

    @Test
    void submitRequest_validDto_savesRequestWithStatusRequested() {
        when(agriInputRepository.findById(10L)).thenReturn(Optional.of(sampleInput));
        InputProcurementRequestDTO dto = new InputProcurementRequestDTO(
                100L, 10L, 50, LocalDate.now(), 5L, 1000.0);

        MessageResponseDTO response = inputRequestService.submitRequest(dto);

        verify(inputRequestRepository).save(any(InputRequest.class));
        assertThat(response.getMessage()).contains("submitted successfully");
    }

    @Test
    void submitRequest_inputNotFound_throwsResourceNotFoundException() {
        when(agriInputRepository.findById(99L)).thenReturn(Optional.empty());
        InputProcurementRequestDTO dto = new InputProcurementRequestDTO(
                100L, 99L, 10, LocalDate.now(), 5L, 500.0);

        assertThatThrownBy(() -> inputRequestService.submitRequest(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ─── updateRequest ───────────────────────────────────────────────────────────

    @Test
    void updateRequest_existingId_updatesProvidedFields() {
        when(inputRequestRepository.findById(1L)).thenReturn(Optional.of(requestedRequest));
        UpdateInputRequestDTO dto = new UpdateInputRequestDTO(80, 7L, 1200.0);

        MessageResponseDTO response = inputRequestService.updateRequest(1L, dto);

        assertThat(requestedRequest.getQuantityRequested()).isEqualTo(80);
        assertThat(requestedRequest.getAssignedCentreId()).isEqualTo(7L);
        assertThat(requestedRequest.getActualPrice()).isEqualTo(1200.0);
        assertThat(response.getMessage()).contains("updated successfully");
    }

    @Test
    void updateRequest_nullFields_doesNotOverwriteExistingValues() {
        when(inputRequestRepository.findById(1L)).thenReturn(Optional.of(requestedRequest));

        inputRequestService.updateRequest(1L, new UpdateInputRequestDTO(null, null, null));

        assertThat(requestedRequest.getQuantityRequested()).isEqualTo(50);
        assertThat(requestedRequest.getAssignedCentreId()).isEqualTo(5L);
    }

    // ─── approveRequest ───────────────────────────────────────────────────────────

    @Test
    void approveRequest_requestedStatus_approvesSuccessfully() {
        when(inputRequestRepository.findById(1L)).thenReturn(Optional.of(requestedRequest));
        ApproveRequestDTO dto = new ApproveRequestDTO(5L, 1000.0, "officer_id_45");

        MessageResponseDTO response = inputRequestService.approveRequest(1L, dto);

        assertThat(requestedRequest.getStatus()).isEqualTo("Approved");
        assertThat(requestedRequest.getAssignedCentreId()).isEqualTo(5L);
        assertThat(requestedRequest.getActualPrice()).isEqualTo(1000.0);
        assertThat(requestedRequest.getApprovedBy()).isEqualTo("officer_id_45");
        assertThat(response.getMessage()).contains("approved successfully");
    }

    @Test
    void approveRequest_nonRequestedStatus_throwsStateConflictException() {
        when(inputRequestRepository.findById(2L)).thenReturn(Optional.of(approvedRequest));

        assertThatThrownBy(() -> inputRequestService.approveRequest(2L, new ApproveRequestDTO(5L, 500.0, "officer_id_45")))
                .isInstanceOf(StateConflictException.class)
                .hasMessageContaining("Approved");
    }

    @Test
    void approveRequest_nonExistingId_throwsResourceNotFoundException() {
        when(inputRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inputRequestService.approveRequest(99L, new ApproveRequestDTO(1L, 100.0, "officer_id_45")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── dispatchRequest ─────────────────────────────────────────────────────────

    @Test
    void dispatchRequest_approvedStatus_dispatchesSuccessfully() {
        when(inputRequestRepository.findById(2L)).thenReturn(Optional.of(approvedRequest));

        MessageResponseDTO response = inputRequestService.dispatchRequest(
                2L, new DispatchRequestDTO("staff_id_9", LocalDate.now()));

        assertThat(approvedRequest.getStatus()).isEqualTo("Dispatched");
        assertThat(approvedRequest.getDispatchedBy()).isEqualTo("staff_id_9");
        assertThat(response.getMessage()).contains("Dispatched");
    }

    @Test
    void dispatchRequest_nonApprovedStatus_throwsStateConflictException() {
        when(inputRequestRepository.findById(1L)).thenReturn(Optional.of(requestedRequest));

        assertThatThrownBy(() -> inputRequestService.dispatchRequest(
                1L, new DispatchRequestDTO("staff_id_9", LocalDate.now())))
                .isInstanceOf(StateConflictException.class)
                .hasMessageContaining("Requested");
    }

    // ─── deliverRequest ───────────────────────────────────────────────────────────

    @Test
    void deliverRequest_dispatchedStatus_deliversSuccessfully() {
        when(inputRequestRepository.findById(3L)).thenReturn(Optional.of(dispatchedRequest));

        MessageResponseDTO response = inputRequestService.deliverRequest(
                3L, new DeliverRequestDTO(LocalDate.now(), "farmer_id_501"));

        assertThat(dispatchedRequest.getStatus()).isEqualTo("Delivered");
        assertThat(dispatchedRequest.getReceivedBy()).isEqualTo("farmer_id_501");
        assertThat(response.getMessage()).contains("Delivered");
    }

    @Test
    void deliverRequest_nonDispatchedStatus_throwsStateConflictException() {
        when(inputRequestRepository.findById(2L)).thenReturn(Optional.of(approvedRequest));

        assertThatThrownBy(() -> inputRequestService.deliverRequest(
                2L, new DeliverRequestDTO(LocalDate.now(), "farmer_id_501")))
                .isInstanceOf(StateConflictException.class);
    }

    // ─── cancelRequest ───────────────────────────────────────────────────────────

    @Test
    void cancelRequest_requestedStatus_cancelsSuccessfully() {
        when(inputRequestRepository.findById(1L)).thenReturn(Optional.of(requestedRequest));

        MessageResponseDTO response = inputRequestService.cancelRequest(
                1L, new CancelRequestDTO("Farmer withdrew due to crop change"));

        assertThat(requestedRequest.getStatus()).isEqualTo("Cancelled");
        assertThat(requestedRequest.getCancellationReason()).isEqualTo("Farmer withdrew due to crop change");
        assertThat(response.getMessage()).contains("cancelled successfully");
    }

    @Test
    void cancelRequest_dispatchedStatus_throwsStateConflictException() {
        when(inputRequestRepository.findById(3L)).thenReturn(Optional.of(dispatchedRequest));

        assertThatThrownBy(() -> inputRequestService.cancelRequest(
                3L, new CancelRequestDTO("Farmer withdrew due to crop change")))
                .isInstanceOf(StateConflictException.class)
                .hasMessageContaining("Dispatched");
    }

    @Test
    void cancelRequest_deliveredStatus_throwsStateConflictException() {
        InputRequest delivered = InputRequest.builder().requestId(4L).status("Delivered").build();
        when(inputRequestRepository.findById(4L)).thenReturn(Optional.of(delivered));

        assertThatThrownBy(() -> inputRequestService.cancelRequest(
                4L, new CancelRequestDTO("Farmer withdrew due to crop change")))
                .isInstanceOf(StateConflictException.class)
                .hasMessageContaining("Delivered");
    }

    // ─── deleteRequest ───────────────────────────────────────────────────────────

    @Test
    void deleteRequest_existingId_deletesSuccessfully() {
        when(inputRequestRepository.existsById(1L)).thenReturn(true);

        MessageResponseDTO response = inputRequestService.deleteRequest(1L);

        verify(inputRequestRepository).deleteById(1L);
        assertThat(response.getMessage()).contains("deleted successfully");
    }

    @Test
    void deleteRequest_nonExistingId_throwsResourceNotFoundException() {
        when(inputRequestRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> inputRequestService.deleteRequest(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
