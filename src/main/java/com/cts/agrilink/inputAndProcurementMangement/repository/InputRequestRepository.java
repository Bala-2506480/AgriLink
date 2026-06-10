package com.cts.agrilink.inputAndProcurementMangement.repository;

import com.cts.agrilink.inputAndProcurementMangement.model.InputRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InputRequestRepository extends JpaRepository<InputRequest, Long> {
    List<InputRequest> findByFarmerId(Long farmerId);
    List<InputRequest> findByStatus(String status);
    List<InputRequest> findByAssignedCentreId(Long centreId);
}