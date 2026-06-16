package com.cts.agrilink.service;

import com.cts.agrilink.dto.AgrilinkDto.*;
import com.cts.agrilink.exception.ConflictException;
import com.cts.agrilink.exception.ResourceNotFoundException;
import com.cts.agrilink.model.CropCatalog;
import com.cts.agrilink.model.CropPlan;
import com.cts.agrilink.repository.CropCatalogRepository;
import com.cts.agrilink.repository.CropPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CropCatalogService {

    private static final Logger log = LoggerFactory.getLogger(CropCatalogService.class);

    private final CropCatalogRepository cropCatalogRepository;
    private final CropPlanRepository cropPlanRepository;

    public CropCatalogService(CropCatalogRepository cropCatalogRepository,
                              CropPlanRepository cropPlanRepository) {
        this.cropCatalogRepository = cropCatalogRepository;
        this.cropPlanRepository = cropPlanRepository;
    }

    @Transactional
    public CropCatalogResponse createCatalog(CropCatalogCreateRequest req) {
        log.info("Creating crop catalog: name={}, season={}", req.getCropName(), req.getCropSeason());
        if (cropCatalogRepository.existsByCropNameAndCropSeason(
                req.getCropName(), req.getCropSeason())) {
            log.warn("Duplicate crop: name={}, season={}", req.getCropName(), req.getCropSeason());
            throw new ConflictException("Crop '" + req.getCropName()
                    + "' already exists for season " + req.getCropSeason());
        }
        CropCatalog c = new CropCatalog();
        c.setCropName(req.getCropName());
        c.setCropCategory(req.getCropCategory());
        c.setCropSeason(req.getCropSeason());
        c.setTypicalDurationDays(req.getTypicalDurationDays());
        c.setExpectedYieldPerAcre(req.getExpectedYieldPerAcre());
        c.setCatalogStatus(req.getCatalogStatus() != null
                ? req.getCatalogStatus() : CropCatalog.CatalogStatus.Ac);
        CropCatalogResponse saved = CropCatalogResponse.from(cropCatalogRepository.save(c));
        log.info("Crop catalog saved: cropId={}", saved.getCropId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<CropCatalogResponse> fetchCatalogs(CropCatalog.CatalogStatus status) {
        log.info("Fetching crop catalogs, status filter={}", status);
        List<CropCatalog> list = status != null
                ? cropCatalogRepository.findByCatalogStatus(status)
                : cropCatalogRepository.findAll();
        if (list.isEmpty()) {
            log.warn("No crops found");
            throw new ResourceNotFoundException("No crops found");
        }
        log.info("Fetched {} crops", list.size());
        return list.stream().map(CropCatalogResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CropCatalogResponse fetchCatalogById(Integer cropId) {
        log.info("Fetching crop by id={}", cropId);
        return CropCatalogResponse.from(
            cropCatalogRepository.findById(cropId)
                .orElseThrow(() -> {
                    log.warn("Crop not found: id={}", cropId);
                    return new ResourceNotFoundException("Crop not found");
                }));
    }

    @Transactional(readOnly = true)
    public List<CropCatalogResponse> fetchBySeason(CropCatalog.CropSeason season) {
        log.info("Fetching crops by season={}", season);
        List<CropCatalog> list = cropCatalogRepository.findByCropSeason(season);
        if (list.isEmpty()) {
            log.warn("No crops found for season={}", season);
            throw new ResourceNotFoundException("No crops found for season: " + season);
        }
        return list.stream().map(CropCatalogResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CropCatalogResponse> fetchByCategory(CropCatalog.CropCategory category) {
        log.info("Fetching crops by category={}", category);
        List<CropCatalog> list = cropCatalogRepository.findByCropCategory(category);
        if (list.isEmpty()) {
            log.warn("No crops found for category={}", category);
            throw new ResourceNotFoundException("No crops found for category: " + category);
        }
        return list.stream().map(CropCatalogResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public CropCatalogResponse updateCatalog(Integer cropId, CropCatalogUpdateRequest req) {
        log.info("Updating crop catalog: cropId={}", cropId);
        CropCatalog c = cropCatalogRepository.findById(cropId)
                .orElseThrow(() -> {
                    log.warn("Crop not found for update: id={}", cropId);
                    return new ResourceNotFoundException("Crop not found");
                });
        if (req.getTypicalDurationDays() != null)
            c.setTypicalDurationDays(req.getTypicalDurationDays());
        if (req.getExpectedYieldPerAcre() != null)
            c.setExpectedYieldPerAcre(req.getExpectedYieldPerAcre());
        if (req.getCatalogStatus() != null) {
            if (c.getCatalogStatus() == req.getCatalogStatus())
                throw new ConflictException("Status conflict, transition not allowed");
            c.setCatalogStatus(req.getCatalogStatus());
        }
        log.info("Crop catalog updated: cropId={}", cropId);
        return CropCatalogResponse.from(cropCatalogRepository.save(c));
    }

    @Transactional
    public void deleteCatalog(Integer cropId) {
        log.info("Deleting crop catalog: cropId={}", cropId);
        CropCatalog c = cropCatalogRepository.findById(cropId)
                .orElseThrow(() -> {
                    log.warn("Crop not found for delete: id={}", cropId);
                    return new ResourceNotFoundException("Crop not found");
                });
        boolean hasPlans = cropPlanRepository.existsByCropCatalog_CropIdAndPlanStatusNotIn(
                cropId, List.of(CropPlan.PlanStatus.Ca,
                        CropPlan.PlanStatus.Fa, CropPlan.PlanStatus.Ha));
        if (hasPlans) {
            log.warn("Cannot delete crop, active plans exist: cropId={}", cropId);
            throw new ConflictException("Crop cannot be deleted, plans exist");
        }
        cropCatalogRepository.delete(c);
        log.info("Crop catalog deleted: cropId={}", cropId);
    }
}