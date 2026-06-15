package com.cts.agrilink.farmerLandRegistration.controller;

import com.cts.agrilink.farmerLandRegistration.dto.ApiResponseDTO;
import com.cts.agrilink.farmerLandRegistration.dto.LandHoldingRequestDTO;
import com.cts.agrilink.farmerLandRegistration.model.LandHolding;
import com.cts.agrilink.farmerLandRegistration.service.LandHoldingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/farmerLandRegistration/landHolding")
public class LandHoldingController {

    @Autowired
    private LandHoldingService landHoldingService;

    @PostMapping("/createLandHolding")
    public ResponseEntity<ApiResponseDTO> createLandHolding(@Valid @RequestBody LandHoldingRequestDTO dto) {
        landHoldingService.createLandHolding(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO("Land holding created successfully"));
    }

    @GetMapping("/fetchLandHoldings")
    public ResponseEntity<List<LandHolding>> fetchAllLandHoldings() {
        return ResponseEntity.ok(landHoldingService.fetchAllLandHoldings());
    }

    @GetMapping("/fetchLandHoldingById/{holdingId}")
    public ResponseEntity<LandHolding> fetchLandHoldingById(@PathVariable Long holdingId) {
        return ResponseEntity.ok(landHoldingService.fetchLandHoldingById(holdingId));
    }

    @GetMapping("/fetchLandHoldingsByFarmer/{farmerId}")
    public ResponseEntity<List<LandHolding>> fetchLandHoldingsByFarmer(@PathVariable Long farmerId) {
        return ResponseEntity.ok(landHoldingService.fetchLandHoldingsByFarmer(farmerId));
    }

    @GetMapping("/fetchLandHoldingsByStatus/{status}")
    public ResponseEntity<List<LandHolding>> fetchLandHoldingsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(landHoldingService.fetchLandHoldingsByStatus(status));
    }

    @PutMapping("/updateLandHolding/{holdingId}")
    public ResponseEntity<ApiResponseDTO> updateLandHolding(
            @PathVariable Long holdingId,
            @RequestBody LandHoldingRequestDTO dto) {
        landHoldingService.updateLandHolding(holdingId, dto);
        return ResponseEntity.ok(new ApiResponseDTO("Land holding updated successfully"));
    }

    // Hard Delete
    @DeleteMapping("/deleteLandHolding/{holdingId}")
    public ResponseEntity<ApiResponseDTO> deleteLandHolding(@PathVariable Long holdingId) {
        ApiResponseDTO response = landHoldingService.deleteLandHolding(holdingId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    // Soft Delete
    @DeleteMapping("/deleteLandHolding/{holdingId}/soft")
    public ResponseEntity<ApiResponseDTO> softDeleteLandHolding(@PathVariable Long holdingId) {
        ApiResponseDTO response = landHoldingService.softDeleteLandHolding(holdingId);
        return ResponseEntity.ok(response);
    }
}
