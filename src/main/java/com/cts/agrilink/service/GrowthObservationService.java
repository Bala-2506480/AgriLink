package com.cts.agrilink.service;

import com.cts.agrilink.dto.AgrilinkDto.*;
import com.cts.agrilink.exception.ConflictException;
import com.cts.agrilink.exception.ResourceNotFoundException;
import com.cts.agrilink.model.CropPlan;
import com.cts.agrilink.model.GrowthObservation;
import com.cts.agrilink.repository.CropPlanRepository;
import com.cts.agrilink.repository.GrowthObservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GrowthObservationService {

    private static final Logger log = LoggerFactory.getLogger(GrowthObservationService.class);

    private final GrowthObservationRepository growthObservationRepository;
    private final CropPlanRepository cropPlanRepository;

    public GrowthObservationService(GrowthObservationRepository growthObservationRepository,
                                    CropPlanRepository cropPlanRepository) {
        this.growthObservationRepository = growthObservationRepository;
        this.cropPlanRepository = cropPlanRepository;
    }

    @Transactional
    public ObservationResponse createObservation(ObservationCreateRequest req) {
        log.info("Creating observation: planId={}, officerId={}, stage={}",
                req.getPlanId(), req.getOfficerId(), req.getGrowthStage());
        CropPlan plan = cropPlanRepository.findById(req.getPlanId())
                .orElseThrow(() -> {
                    log.warn("Invalid planId: {}", req.getPlanId());
                    return new ResourceNotFoundException("Invalid planId: " + req.getPlanId());
                });

        if (plan.getPlanStatus() == CropPlan.PlanStatus.Ha
                || plan.getPlanStatus() == CropPlan.PlanStatus.Fa
                || plan.getPlanStatus() == CropPlan.PlanStatus.Ca) {
            log.warn("Cannot record observation for {} plan: planId={}", plan.getPlanStatus(), req.getPlanId());
            throw new ConflictException(
                    "Cannot record observation for a " + plan.getPlanStatus() + " plan");
        }
        GrowthObservation obs = new GrowthObservation();
        obs.setCropPlan(plan);
        obs.setOfficerId(req.getOfficerId());
        obs.setObservationDate(req.getObservationDate());
        obs.setGrowthStage(req.getGrowthStage());
        obs.setPestOrDiseaseFlag(req.getPestOrDiseaseFlag() != null
                ? req.getPestOrDiseaseFlag() : false);
        obs.setFieldRemarks(req.getFieldRemarks());
        ObservationResponse saved = ObservationResponse.from(growthObservationRepository.save(obs));
        log.info("Observation created: observationId={}", saved.getObservationId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ObservationResponse> fetchObservations() {
        log.info("Fetching all observations");
        List<GrowthObservation> list = growthObservationRepository.findAll();
        if (list.isEmpty()) {
            log.warn("No observations found");
            throw new ResourceNotFoundException("No observations found");
        }
        log.info("Fetched {} observations", list.size());
        return list.stream().map(ObservationResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ObservationResponse fetchObservationById(Integer obsId) {
        log.info("Fetching observation by id={}", obsId);
        return ObservationResponse.from(
            growthObservationRepository.findById(obsId)
                .orElseThrow(() -> {
                    log.warn("Observation not found: id={}", obsId);
                    return new ResourceNotFoundException("Observation not found");
                }));
    }

    @Transactional(readOnly = true)
    public List<ObservationResponse> fetchByPlan(Integer planId) {
        log.info("Fetching observations for planId={}", planId);
        List<GrowthObservation> list =
                growthObservationRepository.findByCropPlan_PlanId(planId);
        if (list.isEmpty()) {
            log.warn("No observations found for planId={}", planId);
            throw new ResourceNotFoundException("No observations found for plan: " + planId);
        }
        return list.stream().map(ObservationResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ObservationResponse> fetchPestAlerts(
            Integer officerId, CropPlan.PlanSeason planSeason, Integer planYear) {
        log.info("Fetching pest alerts: officerId={}, season={}, year={}", officerId, planSeason, planYear);
        List<GrowthObservation> list =
                growthObservationRepository.findPestAlerts(officerId, planSeason, planYear);
        if (list.isEmpty()) {
            log.warn("No pest alerts found");
            throw new ResourceNotFoundException("No pest alerts found");
        }
        return list.stream().map(ObservationResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ObservationResponse> fetchByOfficer(Integer officerId) {
        log.info("Fetching observations for officerId={}", officerId);
        List<GrowthObservation> list =
                growthObservationRepository.findByOfficerId(officerId);
        if (list.isEmpty()) {
            log.warn("No observations found for officerId={}", officerId);
            throw new ResourceNotFoundException("No observations found for officer: " + officerId);
        }
        return list.stream().map(ObservationResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ObservationResponse> fetchByStage(GrowthObservation.GrowthStage stage) {
        log.info("Fetching observations for stage={}", stage);
        List<GrowthObservation> list =
                growthObservationRepository.findByGrowthStage(stage);
        if (list.isEmpty()) {
            log.warn("No observations found for stage={}", stage);
            throw new ResourceNotFoundException("No observations found for stage: " + stage);
        }
        return list.stream().map(ObservationResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public ObservationResponse updateObservation(Integer obsId, ObservationUpdateRequest req) {
        log.info("Updating observation: obsId={}", obsId);
        GrowthObservation obs = growthObservationRepository.findById(obsId)
                .orElseThrow(() -> {
                    log.warn("Observation not found for update: id={}", obsId);
                    return new ResourceNotFoundException("Observation not found");
                });
        if (req.getGrowthStage() != null) obs.setGrowthStage(req.getGrowthStage());
        if (req.getPestOrDiseaseFlag() != null)
            obs.setPestOrDiseaseFlag(req.getPestOrDiseaseFlag());
        if (req.getFieldRemarks() != null) obs.setFieldRemarks(req.getFieldRemarks());
        log.info("Observation updated: obsId={}", obsId);
        return ObservationResponse.from(growthObservationRepository.save(obs));
    }

    @Transactional
    public void updatePestFlag(Integer obsId, ObservationPestFlagRequest req) {
        log.info("Updating pest flag: obsId={}, flag={}", obsId, req.getPestOrDiseaseFlag());
        GrowthObservation obs = growthObservationRepository.findById(obsId)
                .orElseThrow(() -> {
                    log.warn("Observation not found for pest flag update: id={}", obsId);
                    return new ResourceNotFoundException("Observation not found");
                });
        obs.setPestOrDiseaseFlag(req.getPestOrDiseaseFlag());
        growthObservationRepository.save(obs);
        log.info("Pest flag updated: obsId={}", obsId);
    }

    @Transactional
    public void deleteObservation(Integer obsId) {
        log.info("Deleting observation: obsId={}", obsId);
        GrowthObservation obs = growthObservationRepository.findById(obsId)
                .orElseThrow(() -> {
                    log.warn("Observation not found for delete: id={}", obsId);
                    return new ResourceNotFoundException("Observation not found");
                });
        growthObservationRepository.delete(obs);
        log.info("Observation deleted: obsId={}", obsId);
    }
}
