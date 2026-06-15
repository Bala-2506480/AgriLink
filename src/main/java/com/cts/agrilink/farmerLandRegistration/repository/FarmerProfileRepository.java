package com.cts.agrilink.farmerLandRegistration.repository;

import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmerProfileRepository extends JpaRepository<FarmerProfile, Long> {
    boolean existsByNationalIdNumber(String nationalIdNumber);
    List<FarmerProfile> findByUser_UserId(Long userId);
    List<FarmerProfile> findByDistrict(String district);
    List<FarmerProfile> findByStatus(FarmerProfile.FarmerStatus status);
}
