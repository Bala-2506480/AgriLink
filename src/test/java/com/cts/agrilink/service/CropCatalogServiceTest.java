package com.cts.agrilink.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cts.agrilink.dto.AgrilinkDto.*;
import com.cts.agrilink.exception.ConflictException;
import com.cts.agrilink.exception.ResourceNotFoundException;
import com.cts.agrilink.model.CropCatalog;
import com.cts.agrilink.repository.CropCatalogRepository;
import com.cts.agrilink.repository.CropPlanRepository;

@ExtendWith(MockitoExtension.class)
class CropCatalogServiceTest {

    @Mock
    private CropCatalogRepository cropCatalogRepository;

    @Mock
    private CropPlanRepository cropPlanRepository;

    @InjectMocks
    private CropCatalogService cropCatalogService;

    private CropCatalog sampleCatalog;

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
    }

    // Test 1
    @Test
    void createCatalog_success() {
        CropCatalogCreateRequest req = new CropCatalogCreateRequest();
        req.setCropName("Rice");
        req.setCropCategory(CropCatalog.CropCategory.Cereal);
        req.setCropSeason(CropCatalog.CropSeason.Kharif);
        req.setTypicalDurationDays(120);
        req.setExpectedYieldPerAcre(new BigDecimal("25.00"));

        when(cropCatalogRepository.existsByCropNameAndCropSeason("Rice", CropCatalog.CropSeason.Kharif))
                .thenReturn(false);
        when(cropCatalogRepository.save(any(CropCatalog.class))).thenReturn(sampleCatalog);

        CropCatalogResponse response = cropCatalogService.createCatalog(req);

        assertNotNull(response);
        assertEquals("Rice", response.getCropName());
        assertEquals(CropCatalog.CatalogStatus.Ac, response.getCatalogStatus());
        verify(cropCatalogRepository).save(any(CropCatalog.class));
    }

    // Test 2
    @Test
    void createCatalog_throwsConflict_whenDuplicateCropAndSeason() {
        CropCatalogCreateRequest req = new CropCatalogCreateRequest();
        req.setCropName("Rice");
        req.setCropSeason(CropCatalog.CropSeason.Kharif);

        when(cropCatalogRepository.existsByCropNameAndCropSeason("Rice", CropCatalog.CropSeason.Kharif))
                .thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> cropCatalogService.createCatalog(req));
        assertTrue(ex.getMessage().contains("Rice"));
        verify(cropCatalogRepository, never()).save(any());
    }

    // Test 3
    @Test
    void createCatalog_withDefaultStatus_isActive() {
        CropCatalog saved = new CropCatalog();
        saved.setCropId(2);
        saved.setCropName("Wheat");
        saved.setCatalogStatus(CropCatalog.CatalogStatus.Ac);

        when(cropCatalogRepository.existsByCropNameAndCropSeason(any(), any())).thenReturn(false);
        when(cropCatalogRepository.save(any(CropCatalog.class))).thenReturn(saved);

        CropCatalogCreateRequest req = new CropCatalogCreateRequest();
        req.setCropName("Wheat");
        req.setCropCategory(CropCatalog.CropCategory.Cereal);
        req.setCropSeason(CropCatalog.CropSeason.Rabi);
        req.setTypicalDurationDays(110);
        req.setExpectedYieldPerAcre(new BigDecimal("20.00"));

        CropCatalogResponse response = cropCatalogService.createCatalog(req);

        assertEquals(CropCatalog.CatalogStatus.Ac, response.getCatalogStatus());
    }

    // Test 4
    @Test
    void fetchCatalogs_withNoFilter_returnsAll() {
        when(cropCatalogRepository.findAll()).thenReturn(List.of(sampleCatalog));

        List<CropCatalogResponse> result = cropCatalogService.fetchCatalogs(null);

        assertEquals(1, result.size());
        verify(cropCatalogRepository).findAll();
    }

    // Test 5
    @Test
    void fetchCatalogs_withStatusFilter_returnsFiltered() {
        when(cropCatalogRepository.findByCatalogStatus(CropCatalog.CatalogStatus.Ac))
                .thenReturn(List.of(sampleCatalog));

        List<CropCatalogResponse> result = cropCatalogService.fetchCatalogs(CropCatalog.CatalogStatus.Ac);

        assertEquals(1, result.size());
        assertEquals(CropCatalog.CatalogStatus.Ac, result.get(0).getCatalogStatus());
    }

    // Test 6
    @Test
    void fetchCatalogs_throwsNotFound_whenListIsEmpty() {
        when(cropCatalogRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class,
                () -> cropCatalogService.fetchCatalogs(null));
    }

    // Test 7
    @Test
    void fetchCatalogById_success() {
        when(cropCatalogRepository.findById(1)).thenReturn(Optional.of(sampleCatalog));

        CropCatalogResponse response = cropCatalogService.fetchCatalogById(1);

        assertEquals(1, response.getCropId());
        assertEquals("Rice", response.getCropName());
    }

    // Test 8
    @Test
    void fetchCatalogById_throwsNotFound_whenCropDoesNotExist() {
        when(cropCatalogRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cropCatalogService.fetchCatalogById(99));
    }

    // Test 9
    @Test
    void fetchBySeason_success() {
        when(cropCatalogRepository.findByCropSeason(CropCatalog.CropSeason.Kharif))
                .thenReturn(List.of(sampleCatalog));

        List<CropCatalogResponse> result = cropCatalogService.fetchBySeason(CropCatalog.CropSeason.Kharif);

        assertEquals(1, result.size());
        assertEquals(CropCatalog.CropSeason.Kharif, result.get(0).getCropSeason());
    }

    // Test 10
    @Test
    void fetchBySeason_throwsNotFound_whenNoCropsForSeason() {
        when(cropCatalogRepository.findByCropSeason(CropCatalog.CropSeason.Zaid))
                .thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class,
                () -> cropCatalogService.fetchBySeason(CropCatalog.CropSeason.Zaid));
    }

    // Test 11
    @Test
    void fetchByCategory_success() {
        when(cropCatalogRepository.findByCropCategory(CropCatalog.CropCategory.Cereal))
                .thenReturn(List.of(sampleCatalog));

        List<CropCatalogResponse> result = cropCatalogService.fetchByCategory(CropCatalog.CropCategory.Cereal);

        assertEquals(1, result.size());
        assertEquals(CropCatalog.CropCategory.Cereal, result.get(0).getCropCategory());
    }

    // Test 12
    @Test
    void fetchByCategory_throwsNotFound_whenNoCropsForCategory() {
        when(cropCatalogRepository.findByCropCategory(CropCatalog.CropCategory.Spice))
                .thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class,
                () -> cropCatalogService.fetchByCategory(CropCatalog.CropCategory.Spice));
    }

    // Test 13
    @Test
    void updateCatalog_success_updatesAllFields() {
        CropCatalogUpdateRequest req = new CropCatalogUpdateRequest();
        req.setTypicalDurationDays(130);
        req.setExpectedYieldPerAcre(new BigDecimal("27.00"));
        req.setCatalogStatus(CropCatalog.CatalogStatus.In);

        when(cropCatalogRepository.findById(1)).thenReturn(Optional.of(sampleCatalog));
        when(cropCatalogRepository.save(any(CropCatalog.class))).thenReturn(sampleCatalog);

        CropCatalogResponse response = cropCatalogService.updateCatalog(1, req);

        assertNotNull(response);
        verify(cropCatalogRepository).save(sampleCatalog);
    }

    // Test 14
    @Test
    void updateCatalog_throwsNotFound_whenCropDoesNotExist() {
        CropCatalogUpdateRequest req = new CropCatalogUpdateRequest();
        req.setTypicalDurationDays(130);

        when(cropCatalogRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cropCatalogService.updateCatalog(99, req));
        verify(cropCatalogRepository, never()).save(any());
    }

    // Test 15
    @Test
    void updateCatalog_throwsConflict_whenSameStatusProvided() {
        CropCatalogUpdateRequest req = new CropCatalogUpdateRequest();
        req.setCatalogStatus(CropCatalog.CatalogStatus.Ac);

        when(cropCatalogRepository.findById(1)).thenReturn(Optional.of(sampleCatalog));

        assertThrows(ConflictException.class,
                () -> cropCatalogService.updateCatalog(1, req));
        verify(cropCatalogRepository, never()).save(any());
    }
}
