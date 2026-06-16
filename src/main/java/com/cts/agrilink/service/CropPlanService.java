package com.cts.agrilink.service;

import com.cts.agrilink.dto.AgrilinkDto.*;
import com.cts.agrilink.exception.ConflictException;
import com.cts.agrilink.exception.ResourceNotFoundException;
import com.cts.agrilink.model.*;
import com.cts.agrilink.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CropPlanService {

    private final CropPlanRepository cropPlanRepository;
    private final CropCatalogRepository cropCatalogRepository;
    private final GrowthObservationRepository growthObservationRepository;

    public CropPlanService(CropPlanRepository cropPlanRepository,
                           CropCatalogRepository cropCatalogRepository,
                           GrowthObservationRepository growthObservationRepository) {
        this.cropPlanRepository = cropPlanRepository;
        this.cropCatalogRepository = cropCatalogRepository;
        this.growthObservationRepository = growthObservationRepository;
    }

    @Transactional
    public CropPlanResponse createPlan(CropPlanCreateRequest req) {
        CropCatalog cc = cropCatalogRepository.findById(req.getCropId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invalid cropId: " + req.getCropId()));

        if (!req.getExpectedHarvestDate().isAfter(req.getSowingDate()))
            throw new IllegalArgumentException(
                    "expectedHarvestDate must be after sowingDate");

        boolean dup = cropPlanRepository
                .existsByFarmerIdAndHoldingIdAndCropCatalog_CropIdAndPlanSeasonAndPlanYear(
                        req.getFarmerId(), req.getHoldingId(), req.getCropId(),
                        req.getPlanSeason(), req.getPlanYear());
        if (dup)
            throw new ConflictException(
                    "A crop plan already exists for this farmer, holding, crop, season and year");

        CropPlan plan = new CropPlan();
        plan.setFarmerId(req.getFarmerId());
        plan.setHoldingId(req.getHoldingId());
        plan.setCropCatalog(cc);
        plan.setPlanSeason(req.getPlanSeason());
        plan.setPlanYear(req.getPlanYear());
        plan.setSowingDate(req.getSowingDate());
        plan.setExpectedHarvestDate(req.getExpectedHarvestDate());
        plan.setAreaPlanted(req.getAreaPlanted());
        plan.setPlanStatus(req.getPlanStatus() != null
                ? req.getPlanStatus() : CropPlan.PlanStatus.Pl);
        return CropPlanResponse.from(cropPlanRepository.save(plan));
    }

    @Transactional(readOnly = true)
    public List<CropPlanResponse> fetchPlans() {
        List<CropPlan> list = cropPlanRepository.findAll();
        if (list.isEmpty()) throw new ResourceNotFoundException("No plans found");
        return list.stream().map(CropPlanResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CropPlanResponse fetchPlanById(Integer planId) {
        return CropPlanResponse.from(cropPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found")));
    }

    @Transactional(readOnly = true)
    public List<CropPlanResponse> fetchByFarmer(Long farmerId) {
        List<CropPlan> list = cropPlanRepository.findByFarmerId(farmerId);
        if (list.isEmpty())
            throw new ResourceNotFoundException("No plans found for farmer: " + farmerId);
        return list.stream().map(CropPlanResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CropPlanResponse> fetchByStatus(CropPlan.PlanStatus status) {
        List<CropPlan> list = cropPlanRepository.findByPlanStatus(status);
        if (list.isEmpty())
            throw new ResourceNotFoundException("No plans found for status: " + status);
        return list.stream().map(CropPlanResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CropPlanResponse> fetchBySeason(CropPlan.PlanSeason season, Integer year) {
        List<CropPlan> list = cropPlanRepository.findByPlanSeasonAndPlanYear(season, year);
        if (list.isEmpty())
            throw new ResourceNotFoundException("No plans found for season/year");
        return list.stream().map(CropPlanResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public CropPlanResponse updatePlan(Integer planId, CropPlanUpdateRequest req) {
        CropPlan plan = cropPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        if (req.getSowingDate() != null) plan.setSowingDate(req.getSowingDate());
        if (req.getExpectedHarvestDate() != null)
            plan.setExpectedHarvestDate(req.getExpectedHarvestDate());
        if (req.getAreaPlanted() != null) plan.setAreaPlanted(req.getAreaPlanted());
        if (!plan.getExpectedHarvestDate().isAfter(plan.getSowingDate()))
            throw new IllegalArgumentException(
                    "expectedHarvestDate must be after sowingDate");
        return CropPlanResponse.from(cropPlanRepository.save(plan));
    }

    @Transactional
    public void updatePlanStatus(Integer planId, CropPlanStatusRequest req) {
        CropPlan plan = cropPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        if (!plan.getPlanStatus().canTransitionTo(req.getPlanStatus()))
            throw new ConflictException("Status conflict, transition not allowed: "
                    + plan.getPlanStatus() + " → " + req.getPlanStatus());
        plan.setPlanStatus(req.getPlanStatus());
        cropPlanRepository.save(plan);
    }

    @Transactional
    public void approvePlan(Integer planId, CropPlanApproveRequest req) {
        CropPlan plan = cropPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        if (plan.getPlanStatus() != CropPlan.PlanStatus.Pl)
            throw new ConflictException("Only Planned status plans can be approved");

        plan.setApprovedBy(req.getApprovedBy());
        plan.setApprovedAt(LocalDateTime.now());
        plan.setPlanStatus(CropPlan.PlanStatus.So);
        cropPlanRepository.save(plan);
    }

    @Transactional
    public void deletePlan(Integer planId) {
        CropPlan plan = cropPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        if (plan.getPlanStatus() != CropPlan.PlanStatus.Pl)
            throw new ConflictException(
                    "Plan cannot be deleted, only Planned status plans can be deleted");
        if (growthObservationRepository.existsByCropPlan_PlanId(planId))
            throw new ConflictException("Plan cannot be deleted, observations exist");
        cropPlanRepository.delete(plan);
    }
}
