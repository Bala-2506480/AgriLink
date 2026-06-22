package com.cts.agrilink.farmerLandRegistration.repository;

import com.cts.agrilink.farmerLandRegistration.model.LandHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LandHoldingRepository extends JpaRepository<LandHolding, Long> {

    boolean existsBySurveyNumber(String surveyNumber);

    List<LandHolding> findByFarmer_FarmerId(Long farmerId);

    List<LandHolding> findByStatus(LandHolding.Status status);

    boolean existsByFarmer_FarmerId(Long farmerId);
}
