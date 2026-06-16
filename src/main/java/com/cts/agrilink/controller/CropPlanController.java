package com.cts.agrilink.controller;

import com.cts.agrilink.dto.AgrilinkDto.*;
import com.cts.agrilink.model.CropPlan;
import com.cts.agrilink.service.CropPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agriLink/cropSeason")
public class CropPlanController {

    private final CropPlanService cropPlanService;

    public CropPlanController(CropPlanService cropPlanService) {
        this.cropPlanService = cropPlanService;
    }

    @PostMapping("/createPlan")
    public ResponseEntity<Map<String, String>> createPlan(
            @Valid @RequestBody CropPlanCreateRequest request) {
        cropPlanService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Crop plan created successfully"));
    }

    @GetMapping("/fetchPlans")
    public ResponseEntity<List<CropPlanResponse>> fetchPlans() {
        return ResponseEntity.ok(cropPlanService.fetchPlans());
    }

    @GetMapping("/fetchPlanById/{planId}")
    public ResponseEntity<CropPlanResponse> fetchPlanById(@PathVariable Integer planId) {
        return ResponseEntity.ok(cropPlanService.fetchPlanById(planId));
    }

    @GetMapping("/fetchByFarmer/{farmerId}")
    public ResponseEntity<List<CropPlanResponse>> fetchByFarmer(@PathVariable Long farmerId) {
        return ResponseEntity.ok(cropPlanService.fetchByFarmer(farmerId));
    }

    @GetMapping("/fetchByStatus/{planStatus}")
    public ResponseEntity<List<CropPlanResponse>> fetchByStatus(
            @PathVariable CropPlan.PlanStatus planStatus) {
        return ResponseEntity.ok(cropPlanService.fetchByStatus(planStatus));
    }

    @GetMapping("/fetchBySeason/{planSeason}/{planYear}")
    public ResponseEntity<List<CropPlanResponse>> fetchBySeason(
            @PathVariable CropPlan.PlanSeason planSeason,
            @PathVariable Integer planYear) {
        return ResponseEntity.ok(cropPlanService.fetchBySeason(planSeason, planYear));
    }

    @PutMapping("/updatePlan/{planId}")
    public ResponseEntity<Map<String, String>> updatePlan(
            @PathVariable Integer planId,
            @Valid @RequestBody CropPlanUpdateRequest request) {
        cropPlanService.updatePlan(planId, request);
        return ResponseEntity.ok(Map.of("message", "Crop plan updated successfully"));
    }

    @PutMapping("/updatePlanStatus/{planId}")
    public ResponseEntity<Map<String, String>> updatePlanStatus(
            @PathVariable Integer planId,
            @Valid @RequestBody CropPlanStatusRequest request) {
        cropPlanService.updatePlanStatus(planId, request);
        return ResponseEntity.ok(Map.of("message", "Plan status updated successfully"));
    }

    @PutMapping("/approvePlan/{planId}")
    public ResponseEntity<Map<String, String>> approvePlan(
            @PathVariable Integer planId,
            @Valid @RequestBody CropPlanApproveRequest request) {
        cropPlanService.approvePlan(planId, request);
        return ResponseEntity.ok(Map.of("message", "Plan approved successfully"));
    }

    @DeleteMapping("/deletePlan/{planId}")
    public ResponseEntity<Map<String, String>> deletePlan(@PathVariable Integer planId) {
        cropPlanService.deletePlan(planId);
        return ResponseEntity.ok(Map.of("message", "Crop plan deleted successfully"));
    }
}
