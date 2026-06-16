package com.cts.agrilink.controller;

import com.cts.agrilink.dto.AgrilinkDto.*;
import com.cts.agrilink.model.CropPlan;
import com.cts.agrilink.model.GrowthObservation;
import com.cts.agrilink.service.GrowthObservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agriLink/cropSeason")
public class GrowthObservationController {

    private final GrowthObservationService growthObservationService;

    public GrowthObservationController(GrowthObservationService growthObservationService) {
        this.growthObservationService = growthObservationService;
    }

    @PostMapping("/createObservation")
    public ResponseEntity<Map<String, String>> createObservation(
            @Valid @RequestBody ObservationCreateRequest request) {
        growthObservationService.createObservation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Observation recorded successfully"));
    }

    @GetMapping("/fetchObservations")
    public ResponseEntity<List<ObservationResponse>> fetchObservations() {
        return ResponseEntity.ok(growthObservationService.fetchObservations());
    }

    @GetMapping("/fetchObservationById/{observationId}")
    public ResponseEntity<ObservationResponse> fetchObservationById(
            @PathVariable Integer observationId) {
        return ResponseEntity.ok(growthObservationService.fetchObservationById(observationId));
    }

    @GetMapping("/fetchByPlan/{planId}")
    public ResponseEntity<List<ObservationResponse>> fetchByPlan(
            @PathVariable Integer planId) {
        return ResponseEntity.ok(growthObservationService.fetchByPlan(planId));
    }

    @GetMapping("/fetchPestAlerts")
    public ResponseEntity<List<ObservationResponse>> fetchPestAlerts(
            @RequestParam(required = false) Integer officerId,
            @RequestParam(required = false) CropPlan.PlanSeason planSeason,
            @RequestParam(required = false) Integer planYear) {
        return ResponseEntity.ok(
            growthObservationService.fetchPestAlerts(officerId, planSeason, planYear));
    }

    @GetMapping("/fetchByOfficer/{officerId}")
    public ResponseEntity<List<ObservationResponse>> fetchByOfficer(
            @PathVariable Integer officerId) {
        return ResponseEntity.ok(growthObservationService.fetchByOfficer(officerId));
    }

    @GetMapping("/fetchByStage/{growthStage}")
    public ResponseEntity<List<ObservationResponse>> fetchByStage(
            @PathVariable GrowthObservation.GrowthStage growthStage) {
        return ResponseEntity.ok(growthObservationService.fetchByStage(growthStage));
    }

    @PutMapping("/updateObservation/{observationId}")
    public ResponseEntity<Map<String, String>> updateObservation(
            @PathVariable Integer observationId,
            @Valid @RequestBody ObservationUpdateRequest request) {
        growthObservationService.updateObservation(observationId, request);
        return ResponseEntity.ok(Map.of("message", "Growth observation updated successfully"));
    }

    @PutMapping("/updatePestFlag/{observationId}")
    public ResponseEntity<Map<String, String>> updatePestFlag(
            @PathVariable Integer observationId,
            @Valid @RequestBody ObservationPestFlagRequest request) {
        growthObservationService.updatePestFlag(observationId, request);
        return ResponseEntity.ok(Map.of("message", "Pest flag updated successfully"));
    }

    @DeleteMapping("/deleteObservation/{observationId}")
    public ResponseEntity<Map<String, String>> deleteObservation(
            @PathVariable Integer observationId) {
        growthObservationService.deleteObservation(observationId);
        return ResponseEntity.ok(Map.of("message", "Observation deleted successfully"));
    }
}
