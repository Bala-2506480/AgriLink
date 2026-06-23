package com.cts.agrilink.farmerLandRegistration.service;

import com.cts.agrilink.exception.ForbiddenException;
import com.cts.agrilink.exception.ResourceNotFoundException;
import com.cts.agrilink.farmerLandRegistration.dto.*;
import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;
import com.cts.agrilink.farmerLandRegistration.repository.FarmerProfileRepository;
import com.cts.agrilink.farmerLandRegistration.repository.LandHoldingRepository;
import com.cts.agrilink.identityAccess.model.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FarmerService {

    private final FarmerProfileRepository farmerProfileRepository;
    private final LandHoldingRepository   landHoldingRepository;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public FarmerResponseDto createFarmer(CreateFarmerRequestDto dto) {
        if (farmerProfileRepository.existsByNationalIdNumber(dto.getNationalIdNumber())) {
            throw new IllegalStateException("National ID already registered");
        }

        FarmerProfile profile = FarmerProfile.builder()
                .userId(dto.getUserId())
                .name(dto.getName())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .nationalIdNumber(dto.getNationalIdNumber())
                .village(dto.getVillage())
                .district(dto.getDistrict())
                .state(dto.getState())
                .phone(dto.getPhone())
                .bankAccountNumber(dto.getBankAccountNumber())
                .status(FarmerProfile.Status.Active)
                .build();

        return FarmerResponseDto.from(farmerProfileRepository.save(profile));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FarmerResponseDto> fetchAllFarmers() {
        List<FarmerProfile> list = farmerProfileRepository.findAll();
        if (list.isEmpty()) throw new ResourceNotFoundException("No farmers found");
        return list.stream().map(FarmerResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public FarmerResponseDto fetchFarmerById(Long farmerId) {
        return FarmerResponseDto.from(findFarmerOrThrow(farmerId));
    }

    @Transactional(readOnly = true)
    public List<FarmerResponseDto> fetchFarmersByUser(Integer userId) {
        List<FarmerProfile> list = farmerProfileRepository.findByUserId(userId);
        if (list.isEmpty()) throw new ResourceNotFoundException("No farmers found for this user");
        return list.stream().map(FarmerResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<FarmerResponseDto> fetchFarmersByDistrict(String district) {
        List<FarmerProfile> list = farmerProfileRepository.findByDistrict(district);
        if (list.isEmpty()) throw new ResourceNotFoundException("No farmers found in this district");
        return list.stream().map(FarmerResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<FarmerResponseDto> fetchFarmersByStatus(String status) {
        FarmerProfile.Status parsedStatus;
        try {
            parsedStatus = FarmerProfile.Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
        List<FarmerProfile> list = farmerProfileRepository.findByStatus(parsedStatus);
        if (list.isEmpty()) throw new ResourceNotFoundException("No farmers found with this status");
        return list.stream().map(FarmerResponseDto::from).toList();
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public FarmerResponseDto updateFarmer(Long farmerId, UpdateFarmerRequestDto dto,
                                          UserDetails currentUser) {
        FarmerProfile profile = findFarmerOrThrow(farmerId);

        // Farmer role can only update their own profile
        if (isFarmerRole(currentUser) && !profile.getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("Access denied");
        }

        if (dto.getVillage() != null)           profile.setVillage(dto.getVillage());
        if (dto.getDistrict() != null)          profile.setDistrict(dto.getDistrict());
        if (dto.getPhone() != null)             profile.setPhone(dto.getPhone());
        if (dto.getBankAccountNumber() != null) profile.setBankAccountNumber(dto.getBankAccountNumber());
        if (dto.getStatus() != null) {
            if (dto.getStatus() == FarmerProfile.Status.Verified && isFarmerRole(currentUser)) {
                throw new ForbiddenException("Access denied");
            }
            profile.setStatus(dto.getStatus());
        }

        return FarmerResponseDto.from(farmerProfileRepository.save(profile));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteFarmer(Long farmerId) {
        FarmerProfile profile = findFarmerOrThrow(farmerId);
        if (landHoldingRepository.existsByFarmer_FarmerId(farmerId)) {
            throw new IllegalStateException("Farmer cannot be deleted, land holdings exist");
        }
        farmerProfileRepository.delete(profile);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private FarmerProfile findFarmerOrThrow(Long farmerId) {
        return farmerProfileRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));
    }

    private boolean isFarmerRole(UserDetails user) {
        return "Farmer".equals(user.getRole().getRoleName());
    }
}
