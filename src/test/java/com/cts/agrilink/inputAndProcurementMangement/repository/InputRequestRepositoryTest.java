package com.cts.agrilink.inputAndProcurementMangement.repository;

import com.cts.agrilink.inputAndProcurementMangement.model.InputRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class InputRequestRepositoryTest {

    @Autowired
    private InputRequestRepository inputRequestRepository;

    @BeforeEach
    void setUp() {
        inputRequestRepository.deleteAll();

        // Farmer 100 — 2 requests
        inputRequestRepository.save(InputRequest.builder()
                .farmerId(100L).inputId(10L).quantityRequested(50)
                .requestDate(LocalDate.now()).assignedCentreId(5L)
                .actualPrice(1000.0).status("Requested")
                .build());

        inputRequestRepository.save(InputRequest.builder()
                .farmerId(100L).inputId(11L).quantityRequested(30)
                .requestDate(LocalDate.now()).assignedCentreId(5L)
                .actualPrice(600.0).status("Approved")
                .build());

        // Farmer 101 — 1 request at centre 6
        inputRequestRepository.save(InputRequest.builder()
                .farmerId(101L).inputId(10L).quantityRequested(20)
                .requestDate(LocalDate.now()).assignedCentreId(6L)
                .actualPrice(400.0).status("Dispatched")
                .build());

        // Farmer 102 — 1 request
        inputRequestRepository.save(InputRequest.builder()
                .farmerId(102L).inputId(12L).quantityRequested(10)
                .requestDate(LocalDate.now()).assignedCentreId(5L)
                .actualPrice(200.0).status("Delivered")
                .build());
    }

    // ─── findAll ──────────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsAllSavedRequests() {
        List<InputRequest> requests = inputRequestRepository.findAll();

        assertThat(requests).hasSize(4);
    }

    // ─── findById ─────────────────────────────────────────────────────────────────

    @Test
    void findById_existingId_returnsRequest() {
        InputRequest saved = inputRequestRepository.save(InputRequest.builder()
                .farmerId(200L).inputId(20L).quantityRequested(5)
                .requestDate(LocalDate.now()).assignedCentreId(9L)
                .actualPrice(50.0).status("Requested")
                .build());

        Optional<InputRequest> found = inputRequestRepository.findById(saved.getRequestId());

        assertThat(found).isPresent();
        assertThat(found.get().getFarmerId()).isEqualTo(200L);
    }

    @Test
    void findById_nonExistingId_returnsEmpty() {
        Optional<InputRequest> found = inputRequestRepository.findById(9999L);

        assertThat(found).isEmpty();
    }

    // ─── findByFarmerId ───────────────────────────────────────────────────────────

    @Test
    void findByFarmerId_existingFarmer_returnsAllFarmerRequests() {
        List<InputRequest> result = inputRequestRepository.findByFarmerId(100L);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.getFarmerId().equals(100L));
    }

    @Test
    void findByFarmerId_singleRequest_returnsSingleItem() {
        List<InputRequest> result = inputRequestRepository.findByFarmerId(101L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("Dispatched");
    }

    @Test
    void findByFarmerId_nonExistingFarmer_returnsEmptyList() {
        List<InputRequest> result = inputRequestRepository.findByFarmerId(999L);

        assertThat(result).isEmpty();
    }

    // ─── findByStatus ─────────────────────────────────────────────────────────────

    @Test
    void findByStatus_requested_returnsRequestedOnly() {
        List<InputRequest> result = inputRequestRepository.findByStatusIgnoreCase("Requested");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFarmerId()).isEqualTo(100L);
    }

    @Test
    void findByStatus_approved_returnsApprovedOnly() {
        List<InputRequest> result = inputRequestRepository.findByStatusIgnoreCase("Approved");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantityRequested()).isEqualTo(30);
    }

    @Test
    void findByStatus_delivered_returnsDeliveredOnly() {
        List<InputRequest> result = inputRequestRepository.findByStatusIgnoreCase("Delivered");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFarmerId()).isEqualTo(102L);
    }

    @Test
    void findByStatus_cancelled_returnsEmptyList() {
        List<InputRequest> result = inputRequestRepository.findByStatusIgnoreCase("Cancelled");

        assertThat(result).isEmpty();
    }

    // ─── findByAssignedCentreId ───────────────────────────────────────────────────

    @Test
    void findByAssignedCentreId_centre5_returnsThreeRequests() {
        List<InputRequest> result = inputRequestRepository.findByAssignedCentreId(5L);

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(r -> r.getAssignedCentreId().equals(5L));
    }

    @Test
    void findByAssignedCentreId_centre6_returnsOneRequest() {
        List<InputRequest> result = inputRequestRepository.findByAssignedCentreId(6L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFarmerId()).isEqualTo(101L);
    }

    @Test
    void findByAssignedCentreId_nonExistingCentre_returnsEmptyList() {
        List<InputRequest> result = inputRequestRepository.findByAssignedCentreId(999L);

        assertThat(result).isEmpty();
    }

    // ─── save ─────────────────────────────────────────────────────────────────────

    @Test
    void save_newRequest_persistsAndGeneratesId() {
        InputRequest request = InputRequest.builder()
                .farmerId(103L).inputId(15L).quantityRequested(25)
                .requestDate(LocalDate.now()).assignedCentreId(7L)
                .actualPrice(500.0).status("Requested")
                .build();

        InputRequest saved = inputRequestRepository.save(request);

        assertThat(saved.getRequestId()).isNotNull();
        assertThat(inputRequestRepository.findAll()).hasSize(5);
    }

    @Test
    void save_updatesStatusOfExistingRequest() {
        InputRequest request = inputRequestRepository.findByStatusIgnoreCase("Requested").get(0);
        request.setStatus("Approved");
        request.setAssignedCentreId(8L);
        request.setActualPrice(950.0);

        inputRequestRepository.save(request);

        InputRequest updated = inputRequestRepository.findById(request.getRequestId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo("Approved");
        assertThat(updated.getAssignedCentreId()).isEqualTo(8L);
        assertThat(updated.getActualPrice()).isEqualTo(950.0);
    }

    // ─── existsById ───────────────────────────────────────────────────────────────

    @Test
    void existsById_existingId_returnsTrue() {
        Long id = inputRequestRepository.findAll().get(0).getRequestId();

        assertThat(inputRequestRepository.existsById(id)).isTrue();
    }

    @Test
    void existsById_nonExistingId_returnsFalse() {
        assertThat(inputRequestRepository.existsById(9999L)).isFalse();
    }

    // ─── deleteById ───────────────────────────────────────────────────────────────

    @Test
    void deleteById_existingId_removesRecord() {
        Long id = inputRequestRepository.findAll().get(0).getRequestId();

        inputRequestRepository.deleteById(id);

        assertThat(inputRequestRepository.findById(id)).isEmpty();
        assertThat(inputRequestRepository.findAll()).hasSize(3);
    }
}
