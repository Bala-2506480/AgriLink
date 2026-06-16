package com.cts.agrilink.service;

import com.cts.agrilink.dto.AgrilinkDto.*;
import com.cts.agrilink.exception.ConflictException;
import com.cts.agrilink.exception.ResourceNotFoundException;
import com.cts.agrilink.model.CropCatalog;
import com.cts.agrilink.model.CropPlan;
import com.cts.agrilink.repository.CropCatalogRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CropPlanServiceTest {

    @Mock
    private CropPlanRepository cropPlanRepository;

    @Mock
    private CropCatalogRepository cropCatalogRepository;

    @Mock
    private GrowthObservationRepository growthObservationRepository;

    @InjectMocks
    private CropPlanService cropPlanService;

    private CropCatalog sampleCatalog;
    private CropPlan samplePlan;

    @BeforeEach
    void setUp() {
        sampleCatalog = new CropCatalog();
        sampleCatalog.setCropId(1);
        sampleCatalog.setCropName("Rice");
        sampleCatalog.setCropCategory(CropCatalog.CropCategory.Cereal);
        sampleCatalog.setCropSeason(CropCatalog.CropSeason.Kharif);
        sampleCatalog.setTypicalDurationDays(120);
        sampleCatalog.setExpectedYieldPerAcre(new BigDecimal("25.00"));
        sampleCatalog.setCatalogStatus(CropCatalog.CatalogStatus.Ac);

        samplePlan = new CropPlan();
        samplePlan.setPlanId(1);
        samplePlan.setFarmerId(1L);
        samplePlan.setHoldingId(1L);
        samplePlan.setCropCatalog(sampleCatalog);
        samplePlan.setPlanSeason(CropPlan.PlanSeason.Kharif);
        samplePlan.setPlanYear(2026);
        samplePlan.setSowingDate(LocalDate.of(2026, 6, 15));
        samplePlan.setExpectedHarvestDate(LocalDate.of(2026, 10, 15));
        samplePlan.setAreaPlanted(new BigDecimal("3.5"));
        samplePlan.setPlanStatus(CropPlan.PlanStatus.Pl);
    }

    // Test 1
    @Test
    void createPlan_success() {
        CropPlanCreateRequest req = new CropPlanCreateRequest();
        req.setFarmerId(1L);
        req.setHoldingId(1L);
        req.setCropId(1);
        req.setPlanSeason(CropPlan.PlanSeason.Kharif);
        req.setPlanYear(2026);
        req.setSowingDate(LocalDate.of(2026, 6, 15));
        req.setExpectedHarvestDate(LocalDate.of(2026, 10, 15));
        req.setAreaPlanted(new BigDecimal("3.5"));

        when(cropCatalogRepository.findById(1)).thenReturn(Optional.of(sampleCatalog));
        when(cropPlanRepository.existsByFarmerIdAndHoldingIdAndCropCatalog_CropIdAndPlanSeasonAndPlanYear(
                anyLong(), anyLong(), anyInt(), any(), anyInt())).thenReturn(false);
        when(cropPlanRepository.save(any(CropPlan.class))).thenReturn(samplePlan);

        CropPlanResponse response = cropPlanService.createPlan(req);

        assertNotNull(response);
        assertEquals(1, response.getPlanId());
        assertEquals(CropPlan.PlanStatus.Pl, response.getPlanStatus());
        verify(cropPlanRepository).save(any(CropPlan.class));
    }

    // Test 2
    @Test
    void createPlan_throwsNotFound_whenCropIdInvalid() {
        CropPlanCreateRequest req = new CropPlanCreateRequest();
        req.setCropId(99);
        req.setSowingDate(LocalDate.of(2026, 6, 15));
        req.setExpectedHarvestDate(LocalDate.of(2026, 10, 15));

        when(cropCatalogRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cropPlanService.createPlan(req));
        verify(cropPlanRepository, never()).save(any());
    }

    // Test 3
    @Test
    void createPlan_throwsIllegalArgument_whenHarvestDateBeforeSowingDate() {
        CropPlanCreateRequest req = new CropPlanCreateRequest();
        req.setCropId(1);
        req.setSowingDate(LocalDate.of(2026, 10, 15));
        req.setExpectedHarvestDate(LocalDate.of(2026, 6, 15));

        when(cropCatalogRepository.findById(1)).thenReturn(Optional.of(sampleCatalog));

        assertThrows(IllegalArgumentException.class,
                () -> cropPlanService.createPlan(req));
        verify(cropPlanRepository, never()).save(any());
    }

    // Test 4
    @Test
    void createPlan_throwsIllegalArgument_whenHarvestDateEqualsSowingDate() {
        CropPlanCreateRequest req = new CropPlanCreateRequest();
        req.setCropId(1);
        LocalDate same = LocalDate.of(2026, 6, 15);
        req.setSowingDate(same);
        req.setExpectedHarvestDate(same);

        when(cropCatalogRepository.findById(1)).thenReturn(Optional.of(sampleCatalog));

        assertThrows(IllegalArgumentException.class,
                () -> cropPlanService.createPlan(req));
    }

    // Test 5
    @Test
    void createPlan_throwsConflict_whenDuplicatePlanExists() {
        CropPlanCreateRequest req = new CropPlanCreateRequest();
        req.setFarmerId(1L);
        req.setHoldingId(1L);
        req.setCropId(1);
        req.setPlanSeason(CropPlan.PlanSeason.Kharif);
        req.setPlanYear(2026);
        req.setSowingDate(LocalDate.of(2026, 6, 15));
        req.setExpectedHarvestDate(LocalDate.of(2026, 10, 15));

        when(cropCatalogRepository.findById(1)).thenReturn(Optional.of(sampleCatalog));
        when(cropPlanRepository.existsByFarmerIdAndHoldingIdAndCropCatalog_CropIdAndPlanSeasonAndPlanYear(
                anyLong(), anyLong(), anyInt(), any(), anyInt())).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> cropPlanService.createPlan(req));
        verify(cropPlanRepository, never()).save(any());
    }

    // Test 6
    @Test
    void fetchPlans_success() {
        when(cropPlanRepository.findAll()).thenReturn(List.of(samplePlan));

        List<CropPlanResponse> result = cropPlanService.fetchPlans();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getPlanId());
    }

    // Test 7
    @Test
    void fetchPlans_throwsNotFound_whenEmpty() {
        when(cropPlanRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class,
                () -> cropPlanService.fetchPlans());
    }

    // Test 8
    @Test
    void fetchPlanById_success() {
        when(cropPlanRepository.findById(1)).thenReturn(Optional.of(samplePlan));

        CropPlanResponse response = cropPlanService.fetchPlanById(1);

        assertEquals(1, response.getPlanId());
        assertEquals("Rice", response.getCropName());
    }

    // Test 9
    @Test
    void fetchPlanById_throwsNotFound_whenPlanDoesNotExist() {
        when(cropPlanRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cropPlanService.fetchPlanById(99));
    }

    // Test 10
    @Test
    void fetchByFarmer_success() {
        when(cropPlanRepository.findByFarmerId(1L)).thenReturn(List.of(samplePlan));

        List<CropPlanResponse> result = cropPlanService.fetchByFarmer(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getFarmerId());
    }

    // Test 11
    @Test
    void fetchByFarmer_throwsNotFound_whenNoPlansForFarmer() {
        when(cropPlanRepository.findByFarmerId(99L)).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class,
                () -> cropPlanService.fetchByFarmer(99L));
    }

    // Test 12
    @Test
    void fetchByStatus_success() {
        when(cropPlanRepository.findByPlanStatus(CropPlan.PlanStatus.Pl))
                .thenReturn(List.of(samplePlan));

        List<CropPlanResponse> result = cropPlanService.fetchByStatus(CropPlan.PlanStatus.Pl);

        assertEquals(1, result.size());
        assertEquals(CropPlan.PlanStatus.Pl, result.get(0).getPlanStatus());
    }

    // Test 13
    @Test
    void fetchByStatus_throwsNotFound_whenNoPlansForStatus() {
        when(cropPlanRepository.findByPlanStatus(CropPlan.PlanStatus.Ha))
                .thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class,
                () -> cropPlanService.fetchByStatus(CropPlan.PlanStatus.Ha));
    }

    // Test 14
    @Test
    void fetchBySeason_success() {
        when(cropPlanRepository.findByPlanSeasonAndPlanYear(CropPlan.PlanSeason.Kharif, 2026))
                .thenReturn(List.of(samplePlan));

        List<CropPlanResponse> result = cropPlanService.fetchBySeason(CropPlan.PlanSeason.Kharif, 2026);

        assertEquals(1, result.size());
        assertEquals(CropPlan.PlanSeason.Kharif, result.get(0).getPlanSeason());
    }

    // Test 15
    @Test
    void fetchBySeason_throwsNotFound_whenNoPlansForSeasonYear() {
        when(cropPlanRepository.findByPlanSeasonAndPlanYear(CropPlan.PlanSeason.Rabi, 2020))
                .thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class,
                () -> cropPlanService.fetchBySeason(CropPlan.PlanSeason.Rabi, 2020));
    }

    // Test 16
    @Test
    void updatePlan_success() {
        CropPlanUpdateRequest req = new CropPlanUpdateRequest();
        req.setSowingDate(LocalDate.of(2026, 6, 20));
        req.setExpectedHarvestDate(LocalDate.of(2026, 10, 20));
        req.setAreaPlanted(new BigDecimal("4.0"));

        when(cropPlanRepository.findById(1)).thenReturn(Optional.of(samplePlan));
        when(cropPlanRepository.save(any(CropPlan.class))).thenReturn(samplePlan);

        CropPlanResponse response = cropPlanService.updatePlan(1, req);

        assertNotNull(response);
        verify(cropPlanRepository).save(samplePlan);
    }

    // Test 17
    @Test
    void updatePlan_throwsNotFound_whenPlanDoesNotExist() {
        CropPlanUpdateRequest req = new CropPlanUpdateRequest();
        req.setSowingDate(LocalDate.of(2026, 6, 20));
        req.setExpectedHarvestDate(LocalDate.of(2026, 10, 20));

        when(cropPlanRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cropPlanService.updatePlan(99, req));
    }

    // Test 18
    @Test
    void updatePlanStatus_success_PlToSo() {
        CropPlanStatusRequest req = new CropPlanStatusRequest();
        req.setPlanStatus(CropPlan.PlanStatus.So);

        when(cropPlanRepository.findById(1)).thenReturn(Optional.of(samplePlan));
        when(cropPlanRepository.save(any(CropPlan.class))).thenReturn(samplePlan);

        cropPlanService.updatePlanStatus(1, req);

        assertEquals(CropPlan.PlanStatus.So, samplePlan.getPlanStatus());
        verify(cropPlanRepository).save(samplePlan);
    }

    // Test 19
    @Test
    void updatePlanStatus_success_SoToGr() {
        samplePlan.setPlanStatus(CropPlan.PlanStatus.So);
        CropPlanStatusRequest req = new CropPlanStatusRequest();
        req.setPlanStatus(CropPlan.PlanStatus.Gr);

        when(cropPlanRepository.findById(1)).thenReturn(Optional.of(samplePlan));
        when(cropPlanRepository.save(any(CropPlan.class))).thenReturn(samplePlan);

        cropPlanService.updatePlanStatus(1, req);

        assertEquals(CropPlan.PlanStatus.Gr, samplePlan.getPlanStatus());
    }

    // Test 20
    @Test
    void updatePlanStatus_success_GrToHa() {
        samplePlan.setPlanStatus(CropPlan.PlanStatus.Gr);
        CropPlanStatusRequest req = new CropPlanStatusRequest();
        req.setPlanStatus(CropPlan.PlanStatus.Ha);

        when(cropPlanRepository.findById(1)).thenReturn(Optional.of(samplePlan));
        when(cropPlanRepository.save(any(CropPlan.class))).thenReturn(samplePlan);

        cropPlanService.updatePlanStatus(1, req);

        assertEquals(CropPlan.PlanStatus.Ha, samplePlan.getPlanStatus());
    }
}
