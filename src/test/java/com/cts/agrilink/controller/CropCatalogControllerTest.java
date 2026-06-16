package com.cts.agrilink.controller;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//import com.cts.agrilink.dto.AgrilinkDto.*;
import com.cts.agrilink.dto.AgrilinkDto.CropCatalogResponse;
import com.cts.agrilink.exception.ConflictException;
import com.cts.agrilink.exception.GlobalExceptionHandler;
import com.cts.agrilink.exception.ResourceNotFoundException;
import com.cts.agrilink.model.CropCatalog;
import com.cts.agrilink.service.CropCatalogService;

@ExtendWith(MockitoExtension.class)
class CropCatalogControllerTest {

    @Mock
    private CropCatalogService cropCatalogService;

    @InjectMocks
    private CropCatalogController cropCatalogController;

    private MockMvc mockMvc;
    private CropCatalogResponse sample;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(cropCatalogController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sample = new CropCatalogResponse(1, "Rice",
                CropCatalog.CropCategory.Cereal, CropCatalog.CropSeason.Kharif,
                120, new BigDecimal("25.00"), CropCatalog.CatalogStatus.Ac);
    }

    // Test 1
    @Test
    void createCatalog_returns201_withMessageAndData() throws Exception {
        when(cropCatalogService.createCatalog(any())).thenReturn(sample);
        mockMvc.perform(post("/agriLink/cropSeason/createCatalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cropName":"Rice","cropCategory":"Cereal",
                                 "cropSeason":"Kharif","typicalDurationDays":120,
                                 "expectedYieldPerAcre":25.00}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Crop created successfully"));
    }

    // Test 2
    @Test
    void createCatalog_returns400_whenCropNameBlank() throws Exception {
        mockMvc.perform(post("/agriLink/cropSeason/createCatalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cropName":"","cropCategory":"Cereal","cropSeason":"Kharif",
                                 "typicalDurationDays":120,"expectedYieldPerAcre":25.00}"""))
                .andExpect(status().isBadRequest());
    }

    // Test 3
    @Test
    void createCatalog_returns409_whenDuplicateExists() throws Exception {
        when(cropCatalogService.createCatalog(any()))
                .thenThrow(new ConflictException("Crop 'Rice' already exists for season Kharif"));
        mockMvc.perform(post("/agriLink/cropSeason/createCatalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cropName":"Rice","cropCategory":"Cereal","cropSeason":"Kharif",
                                 "typicalDurationDays":120,"expectedYieldPerAcre":25.00}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Rice")));
    }

    // Test 4
    @Test
    void fetchCatalogs_returns200_withList() throws Exception {
        when(cropCatalogService.fetchCatalogs(null)).thenReturn(List.of(sample));
        mockMvc.perform(get("/agriLink/cropSeason/fetchCatalogs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cropName").value("Rice"));
    }

    // Test 5
    @Test
    void fetchCatalogs_returns404_whenEmpty() throws Exception {
        when(cropCatalogService.fetchCatalogs(null))
                .thenThrow(new ResourceNotFoundException("No crops found"));
        mockMvc.perform(get("/agriLink/cropSeason/fetchCatalogs"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No crops found"));
    }

    // Test 6
    @Test
    void fetchCatalogById_returns200() throws Exception {
        when(cropCatalogService.fetchCatalogById(1)).thenReturn(sample);
        mockMvc.perform(get("/agriLink/cropSeason/fetchCatalogById/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cropId").value(1))
                .andExpect(jsonPath("$.cropName").value("Rice"));
    }

    // Test 7
    @Test
    void fetchCatalogById_returns404_whenNotFound() throws Exception {
        when(cropCatalogService.fetchCatalogById(99))
                .thenThrow(new ResourceNotFoundException("Crop not found"));
        mockMvc.perform(get("/agriLink/cropSeason/fetchCatalogById/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Crop not found"));
    }

    // Test 8
    @Test
    void updateCatalog_returns200_withMessage() throws Exception {
        when(cropCatalogService.updateCatalog(eq(1), any())).thenReturn(sample);
        mockMvc.perform(put("/agriLink/cropSeason/updateCatalog/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"typicalDurationDays":130,"expectedYieldPerAcre":27.00}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Crop catalog updated successfully"));
    }

    // Test 9
    @Test
    void updateCatalog_returns404_whenNotFound() throws Exception {
        when(cropCatalogService.updateCatalog(eq(99), any()))
                .thenThrow(new ResourceNotFoundException("Crop not found"));
        mockMvc.perform(put("/agriLink/cropSeason/updateCatalog/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"typicalDurationDays\":130}"))
                .andExpect(status().isNotFound());
    }

    // Test 10
    @Test
    void deleteCatalog_returns200_withMessage() throws Exception {
        doNothing().when(cropCatalogService).deleteCatalog(1);
        mockMvc.perform(delete("/agriLink/cropSeason/deleteCatalog/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Crop deleted successfully"));
    }

    // Test 11
    @Test
    void fetchCatalogs_returns200_filteredByStatus() throws Exception {
        when(cropCatalogService.fetchCatalogs(CropCatalog.CatalogStatus.Ac))
                .thenReturn(List.of(sample));
        mockMvc.perform(get("/agriLink/cropSeason/fetchCatalogs")
                        .param("catalogStatus", "Ac"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].catalogStatus").value("Ac"));
    }

    // Test 12
    @Test
    void fetchBySeason_returns200_withList() throws Exception {
        when(cropCatalogService.fetchBySeason(CropCatalog.CropSeason.Kharif))
                .thenReturn(List.of(sample));
        mockMvc.perform(get("/agriLink/cropSeason/fetchBySeason/Kharif"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cropSeason").value("Kharif"));
    }

    // Test 13
    @Test
    void fetchBySeason_returns404_whenNone() throws Exception {
        when(cropCatalogService.fetchBySeason(CropCatalog.CropSeason.Rabi))
                .thenThrow(new ResourceNotFoundException("No crops found for season: Rabi"));
        mockMvc.perform(get("/agriLink/cropSeason/fetchBySeason/Rabi"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Rabi")));
    }

    // Test 14
    @Test
    void fetchByCategory_returns200_withList() throws Exception {
        when(cropCatalogService.fetchByCategory(CropCatalog.CropCategory.Cereal))
                .thenReturn(List.of(sample));
        mockMvc.perform(get("/agriLink/cropSeason/fetchByCategory/Cereal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cropCategory").value("Cereal"));
    }

    // Test 15
    @Test
    void deleteCatalog_returns409_whenPlansExist() throws Exception {
        doThrow(new ConflictException("Crop cannot be deleted, plans exist"))
                .when(cropCatalogService).deleteCatalog(1);
        mockMvc.perform(delete("/agriLink/cropSeason/deleteCatalog/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Crop cannot be deleted, plans exist"));
    }
}
