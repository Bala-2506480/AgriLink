package com.cts.agrilink.farmerLandRegistration.serviceImpl;

import com.cts.agrilink.farmerLandRegistration.dto.ApiResponseDTO;
import com.cts.agrilink.farmerLandRegistration.dto.FarmerRequestDTO;
import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;
import com.cts.agrilink.farmerLandRegistration.model.User;
import com.cts.agrilink.farmerLandRegistration.service.FarmerService;
import com.cts.agrilink.farmerLandRegistration.repository.FarmerProfileRepository;
import com.cts.agrilink.farmerLandRegistration.repository.LandHoldingRepository;
import com.cts.agrilink.farmerLandRegistration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class FarmerServiceImpl implements FarmerService {

    @Autowired
    private FarmerProfileRepository farmerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LandHoldingRepository landHoldingRepository;

    @Override
    public FarmerProfile createFarmer(FarmerRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid userId"));

        if (farmerProfileRepository.existsByNationalIdNumber(dto.getNationalIdNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "National ID already registered");
        }

        FarmerProfile farmer = FarmerProfile.builder()
                .user(user)
                .name(dto.getName())
                .dateOfBirth(LocalDate.parse(dto.getDateOfBirth()))
                .gender(FarmerProfile.Gender.valueOf(dto.getGender()))
                .nationalIdNumber(dto.getNationalIdNumber())
                .village(dto.getVillage())
                .district(dto.getDistrict())
                .state(dto.getState())
                .phone(dto.getPhone())
                .bankAccountNumber(dto.getBankAccountNumber())
                .status(dto.getStatus() != null
                        ? FarmerProfile.FarmerStatus.valueOf(dto.getStatus())
                        : FarmerProfile.FarmerStatus.Active)
                .build();

        return farmerProfileRepository.save(farmer);
    }

    @Override
    public List<FarmerProfile> fetchAllFarmers() {
        List<FarmerProfile> farmers = farmerProfileRepository.findAll();
        if (farmers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No farmers found");
        }
        return farmers;
    }

    @Override
    public FarmerProfile fetchFarmerById(Long farmerId) {
        return farmerProfileRepository.findById(farmerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Farmer not found"));
    }

    @Override
    public List<FarmerProfile> fetchFarmersByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No farmers found for this user"));

        List<FarmerProfile> farmers = farmerProfileRepository.findByUser_UserId(userId);
        if (farmers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No farmers found for this user");
        }
        return farmers;
    }

    @Override
    public List<FarmerProfile> fetchFarmersByDistrict(String district) {
        List<FarmerProfile> farmers = farmerProfileRepository.findByDistrict(district);
        if (farmers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No farmers found in this district");
        }
        return farmers;
    }

    @Override
    public List<FarmerProfile> fetchFarmersByStatus(String status) {
        FarmerProfile.FarmerStatus farmerStatus;
        try {
            farmerStatus = FarmerProfile.FarmerStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value");
        }

        List<FarmerProfile> farmers = farmerProfileRepository.findByStatus(farmerStatus);
        if (farmers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No farmers found with this status");
        }
        return farmers;
    }

    @Override
    public FarmerProfile updateFarmer(Long farmerId, FarmerRequestDTO dto) {
        FarmerProfile farmer = farmerProfileRepository.findById(farmerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Farmer not found"));

        if (dto.getVillage() != null)           farmer.setVillage(dto.getVillage());
        if (dto.getDistrict() != null)          farmer.setDistrict(dto.getDistrict());
        if (dto.getPhone() != null)             farmer.setPhone(dto.getPhone());
        if (dto.getBankAccountNumber() != null) farmer.setBankAccountNumber(dto.getBankAccountNumber());
        if (dto.getStatus() != null) {
            try {
                farmer.setStatus(FarmerProfile.FarmerStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input");
            }
        }

        return farmerProfileRepository.save(farmer);
    }

    @Override
    public ApiResponseDTO deleteFarmer(Long farmerId) {
        FarmerProfile farmer = farmerProfileRepository.findById(farmerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Farmer not found"));

        if (landHoldingRepository.existsByFarmer_FarmerId(farmerId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Farmer cannot be deleted, land holdings exist");
        }

        farmerProfileRepository.delete(farmer);
        return new ApiResponseDTO("Farmer deleted successfully");
    }

    @Override
    public ApiResponseDTO softDeleteFarmer(Long farmerId) {
        FarmerProfile farmer = farmerProfileRepository.findById(farmerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Farmer not found"));

        farmer.setStatus(FarmerProfile.FarmerStatus.Inactive);
        farmerProfileRepository.save(farmer);
        return new ApiResponseDTO("Farmer deactivated successfully");
    }
}
