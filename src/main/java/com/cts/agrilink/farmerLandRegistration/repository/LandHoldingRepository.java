package com.cts.agrilink.farmerLandRegistration.repository;

import com.cts.agrilink.farmerLandRegistration.model.LandHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LandHoldingRepository extends JpaRepository<LandHolding, Long> {
    boolean existsBySurveyNumber(String surveyNumber);
    List<LandHolding> findByFarmer_FarmerId(Long farmerId);
    List<LandHolding> findByStatus(LandHolding.LandStatus status);
    boolean existsByFarmer_FarmerId(Long farmerId);
}
