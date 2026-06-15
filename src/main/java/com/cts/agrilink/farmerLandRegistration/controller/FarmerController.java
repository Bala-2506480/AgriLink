package com.cts.agrilink.farmerLandRegistration.controller;

import com.cts.agrilink.farmerLandRegistration.dto.ApiResponseDTO;
import com.cts.agrilink.farmerLandRegistration.dto.FarmerRequestDTO;
import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;
import com.cts.agrilink.farmerLandRegistration.service.FarmerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/farmerLandRegistration/farmer")
public class FarmerController {

    @Autowired
    private FarmerService farmerService;

    @PostMapping("/createFarmer")
    public ResponseEntity<ApiResponseDTO> createFarmer(@Valid @RequestBody FarmerRequestDTO dto) {
        farmerService.createFarmer(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO("Farmer profile created successfully"));
    }

    @GetMapping("/fetchFarmers")
    public ResponseEntity<List<FarmerProfile>> fetchAllFarmers() {
        return ResponseEntity.ok(farmerService.fetchAllFarmers());
    }

    @GetMapping("/fetchFarmerById/{farmerId}")
    public ResponseEntity<FarmerProfile> fetchFarmerById(@PathVariable Long farmerId) {
        return ResponseEntity.ok(farmerService.fetchFarmerById(farmerId));
    }

    @GetMapping("/fetchFarmersByUser/{userId}")
    public ResponseEntity<List<FarmerProfile>> fetchFarmersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(farmerService.fetchFarmersByUser(userId));
    }

    @GetMapping("/fetchFarmersByDistrict/{district}")
    public ResponseEntity<List<FarmerProfile>> fetchFarmersByDistrict(@PathVariable String district) {
        return ResponseEntity.ok(farmerService.fetchFarmersByDistrict(district));
    }

    @GetMapping("/fetchFarmersByStatus/{status}")
    public ResponseEntity<List<FarmerProfile>> fetchFarmersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(farmerService.fetchFarmersByStatus(status));
    }

    @PutMapping("/updateFarmer/{farmerId}")
    public ResponseEntity<ApiResponseDTO> updateFarmer(
            @PathVariable Long farmerId,
            @RequestBody FarmerRequestDTO dto) {
        farmerService.updateFarmer(farmerId, dto);
        return ResponseEntity.ok(new ApiResponseDTO("Farmer updated successfully"));
    }

    // Hard Delete
    @DeleteMapping("/deleteFarmer/{farmerId}")
    public ResponseEntity<ApiResponseDTO> deleteFarmer(@PathVariable Long farmerId) {
        ApiResponseDTO response = farmerService.deleteFarmer(farmerId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    // Soft Delete
    @DeleteMapping("/deleteFarmer/{farmerId}/soft")
    public ResponseEntity<ApiResponseDTO> softDeleteFarmer(@PathVariable Long farmerId) {
        ApiResponseDTO response = farmerService.softDeleteFarmer(farmerId);
        return ResponseEntity.ok(response);
    }
}
