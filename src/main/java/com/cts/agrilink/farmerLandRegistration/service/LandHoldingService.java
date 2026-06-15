package com.cts.agrilink.farmerLandRegistration.service;

import com.cts.agrilink.farmerLandRegistration.dto.ApiResponseDTO;
import com.cts.agrilink.farmerLandRegistration.dto.LandHoldingRequestDTO;
import com.cts.agrilink.farmerLandRegistration.model.LandHolding;

import java.util.List;

public interface LandHoldingService {
    LandHolding createLandHolding(LandHoldingRequestDTO dto);
    List<LandHolding> fetchAllLandHoldings();
    LandHolding fetchLandHoldingById(Long holdingId);
    List<LandHolding> fetchLandHoldingsByFarmer(Long farmerId);
    List<LandHolding> fetchLandHoldingsByStatus(String status);
    LandHolding updateLandHolding(Long holdingId, LandHoldingRequestDTO dto);
    ApiResponseDTO deleteLandHolding(Long holdingId);         // Hard Delete
    ApiResponseDTO softDeleteLandHolding(Long holdingId);     // Soft Delete
}
