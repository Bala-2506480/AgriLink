package com.cts.agrilink.farmerLandRegistration.serviceImpl;

import com.cts.agrilink.farmerLandRegistration.dto.ApiResponseDTO;
import com.cts.agrilink.farmerLandRegistration.dto.LandHoldingRequestDTO;
import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;
import com.cts.agrilink.farmerLandRegistration.model.LandHolding;
import com.cts.agrilink.farmerLandRegistration.service.LandHoldingService;
import com.cts.agrilink.farmerLandRegistration.repository.FarmerProfileRepository;
import com.cts.agrilink.farmerLandRegistration.repository.LandHoldingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LandHoldingServiceImpl implements LandHoldingService {

    @Autowired
    private LandHoldingRepository landHoldingRepository;

    @Autowired
    private FarmerProfileRepository farmerProfileRepository;

    @Override
    public LandHolding createLandHolding(LandHoldingRequestDTO dto) {
        FarmerProfile farmer = farmerProfileRepository.findById(dto.getFarmerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid farmerId"));

        if (landHoldingRepository.existsBySurveyNumber(dto.getSurveyNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Survey number already exists");
        }

        LandHolding landHolding = LandHolding.builder()
                .farmer(farmer)
                .surveyNumber(dto.getSurveyNumber())
                .areaAcres(dto.getAreaAcres())
                .soilType(LandHolding.SoilType.valueOf(dto.getSoilType()))
                .irrigationSource(LandHolding.IrrigationSource.valueOf(dto.getIrrigationSource()))
                .ownershipType(LandHolding.OwnershipType.valueOf(dto.getOwnershipType()))
                .status(dto.getStatus() != null
                        ? LandHolding.LandStatus.valueOf(dto.getStatus())
                        : LandHolding.LandStatus.Active)
                .build();

        return landHoldingRepository.save(landHolding);
    }

    @Override
    public List<LandHolding> fetchAllLandHoldings() {
        List<LandHolding> holdings = landHoldingRepository.findAll();
        if (holdings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No land holdings found");
        }
        return holdings;
    }

    @Override
    public LandHolding fetchLandHoldingById(Long holdingId) {
        return landHoldingRepository.findById(holdingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Land holding not found"));
    }

    @Override
    public List<LandHolding> fetchLandHoldingsByFarmer(Long farmerId) {
        farmerProfileRepository.findById(farmerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No land holdings found for this farmer"));

        List<LandHolding> holdings = landHoldingRepository.findByFarmer_FarmerId(farmerId);
        if (holdings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No land holdings found for this farmer");
        }
        return holdings;
    }

    @Override
    public List<LandHolding> fetchLandHoldingsByStatus(String status) {
        LandHolding.LandStatus landStatus;
        try {
            landStatus = LandHolding.LandStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value");
        }

        List<LandHolding> holdings = landHoldingRepository.findByStatus(landStatus);
        if (holdings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No land holdings found with this status");
        }
        return holdings;
    }

    @Override
    public LandHolding updateLandHolding(Long holdingId, LandHoldingRequestDTO dto) {
        LandHolding holding = landHoldingRepository.findById(holdingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Land holding not found"));

        if (dto.getAreaAcres() != null) holding.setAreaAcres(dto.getAreaAcres());
        if (dto.getSoilType() != null) {
            try {
                holding.setSoilType(LandHolding.SoilType.valueOf(dto.getSoilType()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input");
            }
        }
        if (dto.getIrrigationSource() != null) {
            try {
                holding.setIrrigationSource(LandHolding.IrrigationSource.valueOf(dto.getIrrigationSource()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input");
            }
        }
        if (dto.getOwnershipType() != null) {
            try {
                holding.setOwnershipType(LandHolding.OwnershipType.valueOf(dto.getOwnershipType()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input");
            }
        }
        if (dto.getStatus() != null) {
            try {
                holding.setStatus(LandHolding.LandStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input");
            }
        }

        return landHoldingRepository.save(holding);
    }

    @Override
    public ApiResponseDTO deleteLandHolding(Long holdingId) {
        LandHolding holding = landHoldingRepository.findById(holdingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Land holding not found"));

        landHoldingRepository.delete(holding);
        return new ApiResponseDTO("Land holding deleted successfully");
    }

    @Override
    public ApiResponseDTO softDeleteLandHolding(Long holdingId) {
        LandHolding holding = landHoldingRepository.findById(holdingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Land holding not found"));

        holding.setStatus(LandHolding.LandStatus.Disputed);
        landHoldingRepository.save(holding);
        return new ApiResponseDTO("Land holding deactivated successfully");
    }
}
