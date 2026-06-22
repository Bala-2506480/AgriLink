package com.cts.agrilink.farmerLandRegistration.controller;

import com.cts.agrilink.farmerLandRegistration.dto.*;
import com.cts.agrilink.farmerLandRegistration.service.FarmerService;
import com.cts.agrilink.identityAccess.model.UserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agriLink/farmerLandRegistration/farmer")
@RequiredArgsConstructor
public class FarmerController {

    private final FarmerService farmerService;

    // POST /agriLink/farmerLandRegistration/farmer/createFarmer
    @PostMapping("/createFarmer")
    public ResponseEntity<Map<String, String>> createFarmer(
            @Valid @RequestBody CreateFarmerRequestDto dto) {
        farmerService.createFarmer(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Farmer profile created successfully"));
    }

    // GET /agriLink/farmerLandRegistration/farmer/fetchFarmers
    @GetMapping("/fetchFarmers")
    public ResponseEntity<List<FarmerResponseDto>> fetchFarmers() {
        return ResponseEntity.ok(farmerService.fetchAllFarmers());
    }

    // GET /agriLink/farmerLandRegistration/farmer/fetchFarmerById/{farmerId}
    @GetMapping("/fetchFarmerById/{farmerId}")
    public ResponseEntity<FarmerResponseDto> fetchFarmerById(@PathVariable Long farmerId) {
        return ResponseEntity.ok(farmerService.fetchFarmerById(farmerId));
    }

    // GET /agriLink/farmerLandRegistration/farmer/fetchFarmersByUser/{userId}
    @GetMapping("/fetchFarmersByUser/{userId}")
    public ResponseEntity<List<FarmerResponseDto>> fetchFarmersByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(farmerService.fetchFarmersByUser(userId));
    }

    // GET /agriLink/farmerLandRegistration/farmer/fetchFarmersByDistrict/{district}
    @GetMapping("/fetchFarmersByDistrict/{district}")
    public ResponseEntity<List<FarmerResponseDto>> fetchFarmersByDistrict(@PathVariable String district) {
        return ResponseEntity.ok(farmerService.fetchFarmersByDistrict(district));
    }

    // GET /agriLink/farmerLandRegistration/farmer/fetchFarmersByStatus/{status}
    @GetMapping("/fetchFarmersByStatus/{status}")
    public ResponseEntity<List<FarmerResponseDto>> fetchFarmersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(farmerService.fetchFarmersByStatus(status));
    }

    // PUT /agriLink/farmerLandRegistration/farmer/updateFarmer/{farmerId}
    @PutMapping("/updateFarmer/{farmerId}")
    public ResponseEntity<Map<String, String>> updateFarmer(
            @PathVariable Long farmerId,
            @Valid @RequestBody UpdateFarmerRequestDto dto,
            @AuthenticationPrincipal UserDetails currentUser) {
        farmerService.updateFarmer(farmerId, dto, currentUser);
        return ResponseEntity.ok(Map.of("message", "Farmer updated successfully"));
    }

    // DELETE /agriLink/farmerLandRegistration/farmer/deleteFarmer/{farmerId}
    @DeleteMapping("/deleteFarmer/{farmerId}")
    public ResponseEntity<Map<String, String>> deleteFarmer(@PathVariable Long farmerId) {
        farmerService.deleteFarmer(farmerId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(Map.of("message", "Farmer deleted successfully"));
    }
}
