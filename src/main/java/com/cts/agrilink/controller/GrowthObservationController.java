package com.cts.agrilink.controller;

import com.cts.agrilink.dto.AgrilinkDto.*;
import com.cts.agrilink.model.CropPlan;
import com.cts.agrilink.model.GrowthObservation;
import com.cts.agrilink.service.GrowthObservationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agriLink/cropSeason")
public class GrowthObservationController {

    private static final Logger log = LoggerFactory.getLogger(GrowthObservationController.class);

    private final GrowthObservationService growthObservationService;

    public GrowthObservationController(GrowthObservationService growthObservationService) {
        this.growthObservationService = growthObservationService;
    }

    @PostMapping("/createObservation")
    public ResponseEntity<Map<String, String>> createObservation(
            @Valid @RequestBody ObservationCreateRequest request) {
        log.info("POST /createObservation - planId={}, officerId={}, stage={}", request.getPlanId(), request.getOfficerId(), request.getGrowthStage());
        growthObservationService.createObservation(request);
        log.info("Observation recorded successfully for planId={}", request.getPlanId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Observation recorded successfully"));
    }

    @GetMapping("/fetchObservations")
    public ResponseEntity<List<ObservationResponse>> fetchObservations() {
        log.info("GET /fetchObservations");
        return ResponseEntity.ok(growthObservationService.fetchObservations());
    }

    @GetMapping("/fetchObservationById/{observationId}")
    public ResponseEntity<ObservationResponse> fetchObservationById(
            @PathVariable Integer observationId) {
        log.info("GET /fetchObservationById/{}", observationId);
        return ResponseEntity.ok(growthObservationService.fetchObservationById(observationId));
    }

    @GetMapping("/fetchByPlan/{planId}")
    public ResponseEntity<List<ObservationResponse>> fetchByPlan(
            @PathVariable Integer planId) {
        log.info("GET /fetchByPlan/{}", planId);
        return ResponseEntity.ok(growthObservationService.fetchByPlan(planId));
    }

    @GetMapping("/fetchPestAlerts")
    public ResponseEntity<List<ObservationResponse>> fetchPestAlerts(
            @RequestParam(required = false) Integer officerId,
            @RequestParam(required = false) CropPlan.PlanSeason planSeason,
            @RequestParam(required = false) Integer planYear) {
        log.info("GET /fetchPestAlerts - officerId={}, season={}, year={}", officerId, planSeason, planYear);
        return ResponseEntity.ok(
            growthObservationService.fetchPestAlerts(officerId, planSeason, planYear));
    }

    @GetMapping("/fetchByOfficer/{officerId}")
    public ResponseEntity<List<ObservationResponse>> fetchByOfficer(
            @PathVariable Integer officerId) {
        log.info("GET /fetchByOfficer/{}", officerId);
        return ResponseEntity.ok(growthObservationService.fetchByOfficer(officerId));
    }

    @GetMapping("/fetchByStage/{growthStage}")
    public ResponseEntity<List<ObservationResponse>> fetchByStage(
            @PathVariable GrowthObservation.GrowthStage growthStage) {
        log.info("GET /fetchByStage/{}", growthStage);
        return ResponseEntity.ok(growthObservationService.fetchByStage(growthStage));
    }

    @PutMapping("/updateObservation/{observationId}")
    public ResponseEntity<Map<String, String>> updateObservation(
            @PathVariable Integer observationId,
            @Valid @RequestBody ObservationUpdateRequest request) {
        log.info("PUT /updateObservation/{}", observationId);
        growthObservationService.updateObservation(observationId, request);
        log.info("Observation updated successfully: observationId={}", observationId);
        return ResponseEntity.ok(Map.of("message", "Growth observation updated successfully"));
    }

    @PutMapping("/updatePestFlag/{observationId}")
    public ResponseEntity<Map<String, String>> updatePestFlag(
            @PathVariable Integer observationId,
            @Valid @RequestBody ObservationPestFlagRequest request) {
        log.info("PUT /updatePestFlag/{} - flag={}", observationId, request.getPestOrDiseaseFlag());
        growthObservationService.updatePestFlag(observationId, request);
        log.info("Pest flag updated: observationId={}", observationId);
        return ResponseEntity.ok(Map.of("message", "Pest flag updated successfully"));
    }

    @DeleteMapping("/deleteObservation/{observationId}")
    public ResponseEntity<Map<String, String>> deleteObservation(
            @PathVariable Integer observationId) {
        log.info("DELETE /deleteObservation/{}", observationId);
        growthObservationService.deleteObservation(observationId);
        log.info("Observation deleted: observationId={}", observationId);
        return ResponseEntity.ok(Map.of("message", "Observation deleted successfully"));
    }
}
