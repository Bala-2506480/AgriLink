package com.cts.agrilink.service;

import com.cts.agrilink.dto.AgrilinkDto.*;
import com.cts.agrilink.exception.ConflictException;
import com.cts.agrilink.exception.ResourceNotFoundException;
import com.cts.agrilink.model.*;
import com.cts.agrilink.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CropPlanService {

    private static final Logger log = LoggerFactory.getLogger(CropPlanService.class);

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
        log.info("Creating crop plan: farmerId={}, cropId={}, season={}, year={}",
                req.getFarmerId(), req.getCropId(), req.getPlanSeason(), req.getPlanYear());
        CropCatalog cc = cropCatalogRepository.findById(req.getCropId())
                .orElseThrow(() -> {
                    log.warn("Invalid cropId: {}", req.getCropId());
                    return new ResourceNotFoundException("Invalid cropId: " + req.getCropId());
                });

        if (!req.getExpectedHarvestDate().isAfter(req.getSowingDate()))
            throw new IllegalArgumentException("expectedHarvestDate must be after sowingDate");

        boolean dup = cropPlanRepository
                .existsByFarmerIdAndHoldingIdAndCropCatalog_CropIdAndPlanSeasonAndPlanYear(
                        req.getFarmerId(), req.getHoldingId(), req.getCropId(),
                        req.getPlanSeason(), req.getPlanYear());
        if (dup) {
            log.warn("Duplicate plan: farmerId={}, cropId={}", req.getFarmerId(), req.getCropId());
            throw new ConflictException(
                    "A crop plan already exists for this farmer, holding, crop, season and year");
        }
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
        CropPlanResponse saved = CropPlanResponse.from(cropPlanRepository.save(plan));
        log.info("Crop plan created: planId={}", saved.getPlanId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<CropPlanResponse> fetchPlans() {
        log.info("Fetching all crop plans");
        List<CropPlan> list = cropPlanRepository.findAll();
        if (list.isEmpty()) {
            log.warn("No plans found");
            throw new ResourceNotFoundException("No plans found");
        }
        log.info("Fetched {} plans", list.size());
        return list.stream().map(CropPlanResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CropPlanResponse fetchPlanById(Integer planId) {
        log.info("Fetching plan by id={}", planId);
        return CropPlanResponse.from(cropPlanRepository.findById(planId)
                .orElseThrow(() -> {
                    log.warn("Plan not found: id={}", planId);
                    return new ResourceNotFoundException("Plan not found");
                }));
    }

    @Transactional(readOnly = true)
    public List<CropPlanResponse> fetchByFarmer(Long farmerId) {
        log.info("Fetching plans for farmerId={}", farmerId);
        List<CropPlan> list = cropPlanRepository.findByFarmerId(farmerId);
        if (list.isEmpty()) {
            log.warn("No plans found for farmerId={}", farmerId);
            throw new ResourceNotFoundException("No plans found for farmer: " + farmerId);
        }
        return list.stream().map(CropPlanResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CropPlanResponse> fetchByStatus(CropPlan.PlanStatus status) {
        log.info("Fetching plans by status={}", status);
        List<CropPlan> list = cropPlanRepository.findByPlanStatus(status);
        if (list.isEmpty()) {
            log.warn("No plans found for status={}", status);
            throw new ResourceNotFoundException("No plans found for status: " + status);
        }
        return list.stream().map(CropPlanResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CropPlanResponse> fetchBySeason(CropPlan.PlanSeason season, Integer year) {
        log.info("Fetching plans by season={}, year={}", season, year);
        List<CropPlan> list = cropPlanRepository.findByPlanSeasonAndPlanYear(season, year);
        if (list.isEmpty()) {
            log.warn("No plans found for season={}, year={}", season, year);
            throw new ResourceNotFoundException("No plans found for season/year");
        }
        return list.stream().map(CropPlanResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public CropPlanResponse updatePlan(Integer planId, CropPlanUpdateRequest req) {
        log.info("Updating plan: planId={}", planId);
        CropPlan plan = cropPlanRepository.findById(planId)
                .orElseThrow(() -> {
                    log.warn("Plan not found for update: id={}", planId);
                    return new ResourceNotFoundException("Plan not found");
                });
        if (req.getSowingDate() != null) plan.setSowingDate(req.getSowingDate());
        if (req.getExpectedHarvestDate() != null)
            plan.setExpectedHarvestDate(req.getExpectedHarvestDate());
        if (req.getAreaPlanted() != null) plan.setAreaPlanted(req.getAreaPlanted());
        if (!plan.getExpectedHarvestDate().isAfter(plan.getSowingDate()))
            throw new IllegalArgumentException("expectedHarvestDate must be after sowingDate");
        log.info("Plan updated: planId={}", planId);
        return CropPlanResponse.from(cropPlanRepository.save(plan));
    }

    @Transactional
    public void updatePlanStatus(Integer planId, CropPlanStatusRequest req) {
        log.info("Updating plan status: planId={}, newStatus={}", planId, req.getPlanStatus());
        CropPlan plan = cropPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        if (!plan.getPlanStatus().canTransitionTo(req.getPlanStatus())) {
            log.warn("Invalid status transition: {} -> {} for planId={}",
                    plan.getPlanStatus(), req.getPlanStatus(), planId);
            throw new ConflictException("Status conflict, transition not allowed: "
                    + plan.getPlanStatus() + " → " + req.getPlanStatus());
        }
        plan.setPlanStatus(req.getPlanStatus());
        cropPlanRepository.save(plan);
        log.info("Plan status updated: planId={}, status={}", planId, req.getPlanStatus());
    }

    @Transactional
    public void approvePlan(Integer planId, CropPlanApproveRequest req) {
        log.info("Approving plan: planId={}, approvedBy={}", planId, req.getApprovedBy());
        CropPlan plan = cropPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        if (plan.getPlanStatus() != CropPlan.PlanStatus.Pl) {
            log.warn("Cannot approve plan in status={}: planId={}", plan.getPlanStatus(), planId);
            throw new ConflictException("Only Planned status plans can be approved");
        }
        plan.setApprovedBy(req.getApprovedBy());
        plan.setApprovedAt(LocalDateTime.now());
        plan.setPlanStatus(CropPlan.PlanStatus.So);
        cropPlanRepository.save(plan);
        log.info("Plan approved: planId={}", planId);
    }

    @Transactional
    public void deletePlan(Integer planId) {
        log.info("Deleting plan: planId={}", planId);
        CropPlan plan = cropPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        if (plan.getPlanStatus() != CropPlan.PlanStatus.Pl) {
            log.warn("Cannot delete plan in status={}: planId={}", plan.getPlanStatus(), planId);
            throw new ConflictException(
                    "Plan cannot be deleted, only Planned status plans can be deleted");
        }
        if (growthObservationRepository.existsByCropPlan_PlanId(planId)) {
            log.warn("Cannot delete plan, observations exist: planId={}", planId);
            throw new ConflictException("Plan cannot be deleted, observations exist");
        }
        cropPlanRepository.delete(plan);
        log.info("Plan deleted: planId={}", planId);
    }
}
