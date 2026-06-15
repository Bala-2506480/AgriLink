package com.cts.agrilink.farmerLandRegistration.service;

import com.cts.agrilink.farmerLandRegistration.dto.ApiResponseDTO;
import com.cts.agrilink.farmerLandRegistration.dto.FarmerRequestDTO;
import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;

import java.util.List;

public interface FarmerService {
    FarmerProfile createFarmer(FarmerRequestDTO dto);
    List<FarmerProfile> fetchAllFarmers();
    FarmerProfile fetchFarmerById(Long farmerId);
    List<FarmerProfile> fetchFarmersByUser(Long userId);
    List<FarmerProfile> fetchFarmersByDistrict(String district);
    List<FarmerProfile> fetchFarmersByStatus(String status);
    FarmerProfile updateFarmer(Long farmerId, FarmerRequestDTO dto);
    ApiResponseDTO deleteFarmer(Long farmerId);         // Hard Delete
    ApiResponseDTO softDeleteFarmer(Long farmerId);     // Soft Delete
}
