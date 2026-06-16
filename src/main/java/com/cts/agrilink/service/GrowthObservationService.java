package com.cts.agrilink.service;

import com.cts.agrilink.dto.AgrilinkDto.*;
import com.cts.agrilink.exception.ConflictException;
import com.cts.agrilink.exception.ResourceNotFoundException;
import com.cts.agrilink.model.CropPlan;
import com.cts.agrilink.model.GrowthObservation;
import com.cts.agrilink.repository.CropPlanRepository;
import com.cts.agrilink.repository.GrowthObservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GrowthObservationService {

    private final GrowthObservationRepository growthObservationRepository;
    private final CropPlanRepository cropPlanRepository;

    public GrowthObservationService(GrowthObservationRepository growthObservationRepository,
                                    CropPlanRepository cropPlanRepository) {
        this.growthObservationRepository = growthObservationRepository;
        this.cropPlanRepository = cropPlanRepository;
    }

    @Transactional
    public ObservationResponse createObservation(ObservationCreateRequest req) {
        CropPlan plan = cropPlanRepository.findById(req.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invalid planId: " + req.getPlanId()));

        if (plan.getPlanStatus() == CropPlan.PlanStatus.Ha
                || plan.getPlanStatus() == CropPlan.PlanStatus.Fa
                || plan.getPlanStatus() == CropPlan.PlanStatus.Ca)
            throw new ConflictException(
                    "Cannot record observation for a " + plan.getPlanStatus() + " plan");

        GrowthObservation obs = new GrowthObservation();
        obs.setCropPlan(plan);
        obs.setOfficerId(req.getOfficerId());
        obs.setObservationDate(req.getObservationDate());
        obs.setGrowthStage(req.getGrowthStage());
        obs.setPestOrDiseaseFlag(req.getPestOrDiseaseFlag() != null
                ? req.getPestOrDiseaseFlag() : false);
        obs.setFieldRemarks(req.getFieldRemarks());
        return ObservationResponse.from(growthObservationRepository.save(obs));
    }

    @Transactional(readOnly = true)
    public List<ObservationResponse> fetchObservations() {
        List<GrowthObservation> list = growthObservationRepository.findAll();
        if (list.isEmpty())
            throw new ResourceNotFoundException("No observations found");
        return list.stream().map(ObservationResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ObservationResponse fetchObservationById(Integer obsId) {
        return ObservationResponse.from(
            growthObservationRepository.findById(obsId)
                .orElseThrow(() -> new ResourceNotFoundException("Observation not found")));
    }

    @Transactional(readOnly = true)
    public List<ObservationResponse> fetchByPlan(Integer planId) {
        List<GrowthObservation> list =
                growthObservationRepository.findByCropPlan_PlanId(planId);
        if (list.isEmpty())
            throw new ResourceNotFoundException(
                    "No observations found for plan: " + planId);
        return list.stream().map(ObservationResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ObservationResponse> fetchPestAlerts(
            Integer officerId, CropPlan.PlanSeason planSeason, Integer planYear) {
        List<GrowthObservation> list =
                growthObservationRepository.findPestAlerts(officerId, planSeason, planYear);
        if (list.isEmpty())
            throw new ResourceNotFoundException("No pest alerts found");
        return list.stream().map(ObservationResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ObservationResponse> fetchByOfficer(Integer officerId) {
        List<GrowthObservation> list =
                growthObservationRepository.findByOfficerId(officerId);
        if (list.isEmpty())
            throw new ResourceNotFoundException(
                    "No observations found for officer: " + officerId);
        return list.stream().map(ObservationResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ObservationResponse> fetchByStage(GrowthObservation.GrowthStage stage) {
        List<GrowthObservation> list =
                growthObservationRepository.findByGrowthStage(stage);
        if (list.isEmpty())
            throw new ResourceNotFoundException(
                    "No observations found for stage: " + stage);
        return list.stream().map(ObservationResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public ObservationResponse updateObservation(
            Integer obsId, ObservationUpdateRequest req) {
        GrowthObservation obs = growthObservationRepository.findById(obsId)
                .orElseThrow(() -> new ResourceNotFoundException("Observation not found"));
        if (req.getGrowthStage() != null) obs.setGrowthStage(req.getGrowthStage());
        if (req.getPestOrDiseaseFlag() != null)
            obs.setPestOrDiseaseFlag(req.getPestOrDiseaseFlag());
        if (req.getFieldRemarks() != null) obs.setFieldRemarks(req.getFieldRemarks());
        return ObservationResponse.from(growthObservationRepository.save(obs));
    }

    @Transactional
    public void updatePestFlag(Integer obsId, ObservationPestFlagRequest req) {
        GrowthObservation obs = growthObservationRepository.findById(obsId)
                .orElseThrow(() -> new ResourceNotFoundException("Observation not found"));
        obs.setPestOrDiseaseFlag(req.getPestOrDiseaseFlag());
        growthObservationRepository.save(obs);
    }

    @Transactional
    public void deleteObservation(Integer obsId) {
        GrowthObservation obs = growthObservationRepository.findById(obsId)
                .orElseThrow(() -> new ResourceNotFoundException("Observation not found"));
        growthObservationRepository.delete(obs);
    }
}
