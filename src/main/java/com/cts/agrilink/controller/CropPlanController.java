package com.cts.agrilink.controller;

import com.cts.agrilink.dto.AgrilinkDto.*;
import com.cts.agrilink.model.CropPlan;
import com.cts.agrilink.service.CropPlanService;
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
public class CropPlanController {

    private static final Logger log = LoggerFactory.getLogger(CropPlanController.class);

    private final CropPlanService cropPlanService;

    public CropPlanController(CropPlanService cropPlanService) {
        this.cropPlanService = cropPlanService;
    }

    @PostMapping("/createPlan")
    public ResponseEntity<Map<String, String>> createPlan(
            @Valid @RequestBody CropPlanCreateRequest request) {
        log.info("POST /createPlan - farmerId={}, cropId={}, season={}", request.getFarmerId(), request.getCropId(), request.getPlanSeason());
        cropPlanService.createPlan(request);
        log.info("Crop plan created successfully for farmerId={}", request.getFarmerId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Crop plan created successfully"));
    }

    @GetMapping("/fetchPlans")
    public ResponseEntity<List<CropPlanResponse>> fetchPlans() {
        log.info("GET /fetchPlans");
        return ResponseEntity.ok(cropPlanService.fetchPlans());
    }

    @GetMapping("/fetchPlanById/{planId}")
    public ResponseEntity<CropPlanResponse> fetchPlanById(@PathVariable Integer planId) {
        log.info("GET /fetchPlanById/{}", planId);
        return ResponseEntity.ok(cropPlanService.fetchPlanById(planId));
    }

    @GetMapping("/fetchByFarmer/{farmerId}")
    public ResponseEntity<List<CropPlanResponse>> fetchByFarmer(@PathVariable Long farmerId) {
        log.info("GET /fetchByFarmer/{}", farmerId);
        return ResponseEntity.ok(cropPlanService.fetchByFarmer(farmerId));
    }

    @GetMapping("/fetchByStatus/{planStatus}")
    public ResponseEntity<List<CropPlanResponse>> fetchByStatus(
            @PathVariable CropPlan.PlanStatus planStatus) {
        log.info("GET /fetchByStatus/{}", planStatus);
        return ResponseEntity.ok(cropPlanService.fetchByStatus(planStatus));
    }

    @GetMapping("/fetchBySeason/{planSeason}/{planYear}")
    public ResponseEntity<List<CropPlanResponse>> fetchBySeason(
            @PathVariable CropPlan.PlanSeason planSeason,
            @PathVariable Integer planYear) {
        log.info("GET /fetchBySeason/{}/{}", planSeason, planYear);
        return ResponseEntity.ok(cropPlanService.fetchBySeason(planSeason, planYear));
    }

    @PutMapping("/updatePlan/{planId}")
    public ResponseEntity<Map<String, String>> updatePlan(
            @PathVariable Integer planId,
            @Valid @RequestBody CropPlanUpdateRequest request) {
        log.info("PUT /updatePlan/{}", planId);
        cropPlanService.updatePlan(planId, request);
        log.info("Crop plan updated successfully: planId={}", planId);
        return ResponseEntity.ok(Map.of("message", "Crop plan updated successfully"));
    }

    @PutMapping("/updatePlanStatus/{planId}")
    public ResponseEntity<Map<String, String>> updatePlanStatus(
            @PathVariable Integer planId,
            @Valid @RequestBody CropPlanStatusRequest request) {
        log.info("PUT /updatePlanStatus/{} - newStatus={}", planId, request.getPlanStatus());
        cropPlanService.updatePlanStatus(planId, request);
        log.info("Plan status updated: planId={}, status={}", planId, request.getPlanStatus());
        return ResponseEntity.ok(Map.of("message", "Plan status updated successfully"));
    }

    @PutMapping("/approvePlan/{planId}")
    public ResponseEntity<Map<String, String>> approvePlan(
            @PathVariable Integer planId,
            @Valid @RequestBody CropPlanApproveRequest request) {
        log.info("PUT /approvePlan/{} - approvedBy={}", planId, request.getApprovedBy());
        cropPlanService.approvePlan(planId, request);
        log.info("Plan approved: planId={}", planId);
        return ResponseEntity.ok(Map.of("message", "Plan approved successfully"));
    }

    @DeleteMapping("/deletePlan/{planId}")
    public ResponseEntity<Map<String, String>> deletePlan(@PathVariable Integer planId) {
        log.info("DELETE /deletePlan/{}", planId);
        cropPlanService.deletePlan(planId);
        log.info("Crop plan deleted: planId={}", planId);
        return ResponseEntity.ok(Map.of("message", "Crop plan deleted successfully"));
    }
}
