package com.cts.agrilink.farmerLandRegistration.service;

import com.cts.agrilink.exception.ForbiddenException;
import com.cts.agrilink.exception.ResourceNotFoundException;
import com.cts.agrilink.farmerLandRegistration.dto.*;
import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;
import com.cts.agrilink.farmerLandRegistration.model.LandHolding;
import com.cts.agrilink.farmerLandRegistration.repository.FarmerProfileRepository;
import com.cts.agrilink.farmerLandRegistration.repository.LandHoldingRepository;
import com.cts.agrilink.identityAccess.model.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LandHoldingService {

    private final LandHoldingRepository   landHoldingRepository;
    private final FarmerProfileRepository farmerProfileRepository;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public LandHoldingResponseDto createLandHolding(CreateLandHoldingRequestDto dto) {
        FarmerProfile farmer = farmerProfileRepository.findById(dto.getFarmerId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid farmerId"));

        if (landHoldingRepository.existsBySurveyNumber(dto.getSurveyNumber())) {
            throw new IllegalStateException("Survey number already exists");
        }

        LandHolding holding = LandHolding.builder()
                .farmer(farmer)
                .surveyNumber(dto.getSurveyNumber())
                .areaAcres(dto.getAreaAcres())
                .soilType(dto.getSoilType())
                .irrigationSource(dto.getIrrigationSource())
                .ownershipType(dto.getOwnershipType())
                .status(LandHolding.Status.Active)
                .build();

        return LandHoldingResponseDto.from(landHoldingRepository.save(holding));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<LandHoldingResponseDto> fetchAllLandHoldings() {
        List<LandHolding> list = landHoldingRepository.findAll();
        if (list.isEmpty()) throw new ResourceNotFoundException("No land holdings found");
        return list.stream().map(LandHoldingResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public LandHoldingResponseDto fetchLandHoldingById(Long holdingId) {
        return LandHoldingResponseDto.from(findHoldingOrThrow(holdingId));
    }

    @Transactional(readOnly = true)
    public List<LandHoldingResponseDto> fetchLandHoldingsByFarmer(Long farmerId,
                                                                   UserDetails currentUser) {
        // Farmer can only view their own holdings
        if (isFarmerRole(currentUser)) {
            FarmerProfile profile = farmerProfileRepository.findById(farmerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));
            if (!profile.getUserId().equals(currentUser.getUserId())) {
                throw new ForbiddenException("Access denied");
            }
        }
        List<LandHolding> list = landHoldingRepository.findByFarmer_FarmerId(farmerId);
        if (list.isEmpty()) throw new ResourceNotFoundException("No land holdings found for this farmer");
        return list.stream().map(LandHoldingResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<LandHoldingResponseDto> fetchLandHoldingsByStatus(String status) {
        LandHolding.Status parsedStatus;
        try {
            parsedStatus = LandHolding.Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
        List<LandHolding> list = landHoldingRepository.findByStatus(parsedStatus);
        if (list.isEmpty()) throw new ResourceNotFoundException("No land holdings found with this status");
        return list.stream().map(LandHoldingResponseDto::from).toList();
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public LandHoldingResponseDto updateLandHolding(Long holdingId, UpdateLandHoldingRequestDto dto) {
        LandHolding holding = findHoldingOrThrow(holdingId);

        if (dto.getAreaAcres() != null)        holding.setAreaAcres(dto.getAreaAcres());
        if (dto.getSoilType() != null)         holding.setSoilType(dto.getSoilType());
        if (dto.getIrrigationSource() != null) holding.setIrrigationSource(dto.getIrrigationSource());
        if (dto.getOwnershipType() != null)    holding.setOwnershipType(dto.getOwnershipType());
        if (dto.getStatus() != null)           holding.setStatus(dto.getStatus());

        return LandHoldingResponseDto.from(landHoldingRepository.save(holding));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteLandHolding(Long holdingId) {
        LandHolding holding = findHoldingOrThrow(holdingId);
        if (holding.getStatus() == LandHolding.Status.Disputed) {
            throw new IllegalStateException("Land holding cannot be deleted");
        }
        landHoldingRepository.delete(holding);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private LandHolding findHoldingOrThrow(Long holdingId) {
        return landHoldingRepository.findById(holdingId)
                .orElseThrow(() -> new ResourceNotFoundException("Land holding not found"));
    }

    private boolean isFarmerRole(UserDetails user) {
        return "Farmer".equals(user.getRole().getRoleName());
    }
}
