package com.cts.agrilink.farmerLandRegistration.controller;

import com.cts.agrilink.farmerLandRegistration.dto.*;
import com.cts.agrilink.farmerLandRegistration.service.LandHoldingService;
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
@RequestMapping("/agriLink/farmerLandRegistration/landHolding")
@RequiredArgsConstructor
public class LandHoldingController {

    private final LandHoldingService landHoldingService;

    // POST /agriLink/farmerLandRegistration/landHolding/createLandHolding
    @PostMapping("/createLandHolding")
    public ResponseEntity<Map<String, String>> createLandHolding(
            @Valid @RequestBody CreateLandHoldingRequestDto dto) {
        landHoldingService.createLandHolding(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Land holding created successfully"));
    }

    // GET /agriLink/farmerLandRegistration/landHolding/fetchLandHoldings
    @GetMapping("/fetchLandHoldings")
    public ResponseEntity<List<LandHoldingResponseDto>> fetchLandHoldings() {
        return ResponseEntity.ok(landHoldingService.fetchAllLandHoldings());
    }

    // GET /agriLink/farmerLandRegistration/landHolding/fetchLandHoldingById/{holdingId}
    @GetMapping("/fetchLandHoldingById/{holdingId}")
    public ResponseEntity<LandHoldingResponseDto> fetchLandHoldingById(@PathVariable Long holdingId) {
        return ResponseEntity.ok(landHoldingService.fetchLandHoldingById(holdingId));
    }

    // GET /agriLink/farmerLandRegistration/landHolding/fetchLandHoldingsByFarmer/{farmerId}
    @GetMapping("/fetchLandHoldingsByFarmer/{farmerId}")
    public ResponseEntity<List<LandHoldingResponseDto>> fetchLandHoldingsByFarmer(
            @PathVariable Long farmerId,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(landHoldingService.fetchLandHoldingsByFarmer(farmerId, currentUser));
    }

    // GET /agriLink/farmerLandRegistration/landHolding/fetchLandHoldingsByStatus/{status}
    @GetMapping("/fetchLandHoldingsByStatus/{status}")
    public ResponseEntity<List<LandHoldingResponseDto>> fetchLandHoldingsByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(landHoldingService.fetchLandHoldingsByStatus(status));
    }

    // PUT /agriLink/farmerLandRegistration/landHolding/updateLandHolding/{holdingId}
    @PutMapping("/updateLandHolding/{holdingId}")
    public ResponseEntity<Map<String, String>> updateLandHolding(
            @PathVariable Long holdingId,
            @Valid @RequestBody UpdateLandHoldingRequestDto dto) {
        landHoldingService.updateLandHolding(holdingId, dto);
        return ResponseEntity.ok(Map.of("message", "Land holding updated successfully"));
    }

    // DELETE /agriLink/farmerLandRegistration/landHolding/deleteLandHolding/{holdingId}
    @DeleteMapping("/deleteLandHolding/{holdingId}")
    public ResponseEntity<Map<String, String>> deleteLandHolding(@PathVariable Long holdingId) {
        landHoldingService.deleteLandHolding(holdingId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(Map.of("message", "Land holding deleted successfully"));
    }
}
