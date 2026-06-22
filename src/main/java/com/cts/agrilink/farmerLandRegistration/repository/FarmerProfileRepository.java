package com.cts.agrilink.farmerLandRegistration.repository;

import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FarmerProfileRepository extends JpaRepository<FarmerProfile, Long> {

    boolean existsByNationalIdNumber(String nationalIdNumber);

    List<FarmerProfile> findByUserId(Integer userId);

    List<FarmerProfile> findByDistrict(String district);

    List<FarmerProfile> findByStatus(FarmerProfile.Status status);
}
