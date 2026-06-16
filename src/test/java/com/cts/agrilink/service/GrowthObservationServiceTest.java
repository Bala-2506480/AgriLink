package com.cts.agrilink.service;

import com.cts.agrilink.dto.AgrilinkDto.*;
import com.cts.agrilink.exception.ConflictException;
import com.cts.agrilink.exception.ResourceNotFoundException;
import com.cts.agrilink.model.CropCatalog;
import com.cts.agrilink.model.CropPlan;
import com.cts.agrilink.model.GrowthObservation;
import com.cts.agrilink.repository.CropPlanRepository;
import com.cts.agrilink.repository.GrowthObservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrowthObservationServiceTest {

    @Mock
    private GrowthObservationRepository growthObservationRepository;

    @Mock
    private CropPlanRepository cropPlanRepository;

    @InjectMocks
    private GrowthObservationService growthObservationService;

    private CropPlan samplePlan;
    private GrowthObservation sampleObservation;

    @BeforeEach
    void setUp() {
        CropCatalog catalog = new CropCatalog();
        catalog.setCropId(1);
        catalog.setCropName("Rice");

        samplePlan = new CropPlan();
        samplePlan.setPlanId(1);
        samplePlan.setFarmerId(1L);
        samplePlan.setHoldingId(1L);
        samplePlan.setCropCatalog(catalog);
        samplePlan.setPlanSeason(CropPlan.PlanSeason.Kharif);
        samplePlan.setPlanYear(2026);
        samplePlan.setSowingDate(LocalDate.of(2026, 6, 15));
        samplePlan.setExpectedHarvestDate(LocalDate.of(2026, 10, 15));
        samplePlan.setAreaPlanted(new BigDecimal("3.5"));
        samplePlan.setPlanStatus(CropPlan.PlanStatus.So);

        sampleObservation = new GrowthObservation();
        sampleObservation.setObservationId(1);
        sampleObservation.setCropPlan(samplePlan);
        sampleObservation.setOfficerId(2);
        sampleObservation.setObservationDate(LocalDate.of(2026, 7, 5));
        sampleObservation.setGrowthStage(GrowthObservation.GrowthStage.Germination);
        sampleObservation.setPestOrDiseaseFlag(false);
        sampleObservation.setFieldRemarks("Healthy germination.");
        sampleObservation.setCreatedAt(LocalDateTime.now());
    }

    // Test 1
    @Test
    void createObservation_success() {
        ObservationCreateRequest req = new ObservationCreateRequest();
        req.setPlanId(1);
        req.setOfficerId(2);
        req.setObservationDate(LocalDate.of(2026, 7, 5));
        req.setGrowthStage(GrowthObservation.GrowthStage.Germination);
        req.setPestOrDiseaseFlag(false);
        req.setFieldRemarks("Healthy germination.");

        when(cropPlanRepository.findById(1)).thenReturn(Optional.of(samplePlan));
        when(growthObservationRepository.save(any(GrowthObservation.class)))
                .thenReturn(sampleObservation);

        ObservationResponse response = growthObservationService.createObservation(req);

        assertNotNull(response);
        assertEquals(1, response.getObservationId());
        assertEquals(GrowthObservation.GrowthStage.Germination, response.getGrowthStage());
        assertFalse(response.getPestOrDiseaseFlag());
        verify(growthObservationRepository).save(any(GrowthObservation.class));
    }

    // Test 2
    @Test
    void createObservation_throwsNotFound_whenPlanIdInvalid() {
        ObservationCreateRequest req = new ObservationCreateRequest();
        req.setPlanId(99);

        when(cropPlanRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> growthObservationService.createObservation(req));
        verify(growthObservationRepository, never()).save(any());
    }

    // Test 3
    @Test
    void createObservation_throwsConflict_whenPlanIsHarvested() {
        samplePlan.setPlanStatus(CropPlan.PlanStatus.Ha);
        ObservationCreateRequest req = new ObservationCreateRequest();
        req.setPlanId(1);

        when(cropPlanRepository.findById(1)).thenReturn(Optional.of(samplePlan));

        assertThrows(ConflictException.class,
                () -> growthObservationService.createObservation(req));
        verify(growthObservationRepository, never()).save(any());
    }

    // Test 4
    @Test
    void createObservation_throwsConflict_whenPlanIsFailed() {
        samplePlan.setPlanStatus(CropPlan.PlanStatus.Fa);
        ObservationCreateRequest req = new ObservationCreateRequest();
        req.setPlanId(1);

        when(cropPlanRepository.findById(1)).thenReturn(Optional.of(samplePlan));

        assertThrows(ConflictException.class,
                () -> growthObservationService.createObservation(req));
    }

    // Test 5
    @Test
    void createObservation_throwsConflict_whenPlanIsCancelled() {
        samplePlan.setPlanStatus(CropPlan.PlanStatus.Ca);
        ObservationCreateRequest req = new ObservationCreateRequest();
        req.setPlanId(1);

        when(cropPlanRepository.findById(1)).thenReturn(Optional.of(samplePlan));

        assertThrows(ConflictException.class,
                () -> growthObservationService.createObservation(req));
    }

    // Test 6
    @Test
    void createObservation_withPestFlag_true() {
        sampleObservation.setPestOrDiseaseFlag(true);
        ObservationCreateRequest req = new ObservationCreateRequest();
        req.setPlanId(1);
        req.setOfficerId(2);
        req.setObservationDate(LocalDate.of(2026, 7, 10));
        req.setGrowthStage(GrowthObservation.GrowthStage.Vegetative);
        req.setPestOrDiseaseFlag(true);

        when(cropPlanRepository.findById(1)).thenReturn(Optional.of(samplePlan));
        when(growthObservationRepository.save(any(GrowthObservation.class)))
                .thenReturn(sampleObservation);

        ObservationResponse response = growthObservationService.createObservation(req);

        assertTrue(response.getPestOrDiseaseFlag());
    }

    // Test 7
    @Test
    void fetchObservations_success() {
        when(growthObservationRepository.findAll()).thenReturn(List.of(sampleObservation));

        List<ObservationResponse> result = growthObservationService.fetchObservations();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getObservationId());
    }

    // Test 8
    @Test
    void fetchObservations_throwsNotFound_whenEmpty() {
        when(growthObservationRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class,
                () -> growthObservationService.fetchObservations());
    }

    // Test 9
    @Test
    void fetchObservationById_success() {
        when(growthObservationRepository.findById(1)).thenReturn(Optional.of(sampleObservation));

        ObservationResponse response = growthObservationService.fetchObservationById(1);

        assertEquals(1, response.getObservationId());
        assertEquals(GrowthObservation.GrowthStage.Germination, response.getGrowthStage());
    }

    // Test 10
    @Test
    void fetchObservationById_throwsNotFound_whenObservationDoesNotExist() {
        when(growthObservationRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> growthObservationService.fetchObservationById(99));
    }

    // Test 11
    @Test
    void fetchByPlan_success() {
        when(growthObservationRepository.findByCropPlan_PlanId(1))
                .thenReturn(List.of(sampleObservation));

        List<ObservationResponse> result = growthObservationService.fetchByPlan(1);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getPlanId());
    }

    // Test 12
    @Test
    void fetchByPlan_throwsNotFound_whenNoObservationsForPlan() {
        when(growthObservationRepository.findByCropPlan_PlanId(99))
                .thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class,
                () -> growthObservationService.fetchByPlan(99));
    }

    // Test 13
    @Test
    void fetchByOfficer_success() {
        when(growthObservationRepository.findByOfficerId(2))
                .thenReturn(List.of(sampleObservation));

        List<ObservationResponse> result = growthObservationService.fetchByOfficer(2);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getOfficerId());
    }

    // Test 14
    @Test
    void fetchByOfficer_throwsNotFound_whenNoObservationsForOfficer() {
        when(growthObservationRepository.findByOfficerId(99))
                .thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class,
                () -> growthObservationService.fetchByOfficer(99));
    }

    // Test 15
    @Test
    void fetchByStage_success() {
        when(growthObservationRepository.findByGrowthStage(GrowthObservation.GrowthStage.Germination))
                .thenReturn(List.of(sampleObservation));

        List<ObservationResponse> result =
                growthObservationService.fetchByStage(GrowthObservation.GrowthStage.Germination);

        assertEquals(1, result.size());
        assertEquals(GrowthObservation.GrowthStage.Germination, result.get(0).getGrowthStage());
    }

    // Test 16
    @Test
    void fetchByStage_throwsNotFound_whenNoObservationsForStage() {
        when(growthObservationRepository.findByGrowthStage(GrowthObservation.GrowthStage.Maturity))
                .thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class,
                () -> growthObservationService.fetchByStage(GrowthObservation.GrowthStage.Maturity));
    }

    // Test 17
    @Test
    void updateObservation_success_updatesAllFields() {
        ObservationUpdateRequest req = new ObservationUpdateRequest();
        req.setGrowthStage(GrowthObservation.GrowthStage.Vegetative);
        req.setPestOrDiseaseFlag(true);
        req.setFieldRemarks("Aphids detected.");

        when(growthObservationRepository.findById(1)).thenReturn(Optional.of(sampleObservation));
        when(growthObservationRepository.save(any(GrowthObservation.class)))
                .thenReturn(sampleObservation);

        ObservationResponse response = growthObservationService.updateObservation(1, req);

        assertNotNull(response);
        verify(growthObservationRepository).save(sampleObservation);
    }
}
