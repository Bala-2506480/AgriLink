package com.cts.agrilink.controller;

import com.cts.agrilink.dto.AgrilinkDto.*;
import com.cts.agrilink.exception.ConflictException;
import com.cts.agrilink.exception.GlobalExceptionHandler;
import com.cts.agrilink.exception.ResourceNotFoundException;
import com.cts.agrilink.model.CropPlan;
import com.cts.agrilink.service.CropPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CropPlanControllerTest {

    @Mock
    private CropPlanService cropPlanService;

    @InjectMocks
    private CropPlanController cropPlanController;

    private MockMvc mockMvc;
    private CropPlanResponse sample;

    private static void set(Object obj, String name, Object value) {
        try {
            java.lang.reflect.Field f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(cropPlanController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sample = new CropPlanResponse();
        set(sample, "planId",              1);
        set(sample, "farmerId",            1L);
        set(sample, "holdingId",           1L);
        set(sample, "cropId",              1);
        set(sample, "cropName",            "Rice");
        set(sample, "planSeason",          CropPlan.PlanSeason.Kharif);
        set(sample, "planYear",            2026);
        set(sample, "sowingDate",          LocalDate.of(2026, 6, 15));
        set(sample, "expectedHarvestDate", LocalDate.of(2026, 10, 15));
        set(sample, "areaPlanted",         new BigDecimal("3.5"));
        set(sample, "planStatus",          CropPlan.PlanStatus.Pl);
    }

    // Test 1
    @Test
    void createPlan_returns201_withMessageAndData() throws Exception {
        when(cropPlanService.createPlan(any())).thenReturn(sample);
        mockMvc.perform(post("/agriLink/cropSeason/createPlan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"farmerId":1,"holdingId":1,"cropId":1,"planSeason":"Kharif",
                                 "planYear":2026,"sowingDate":"2026-06-15",
                                 "expectedHarvestDate":"2026-10-15","areaPlanted":3.5}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Crop plan created successfully"));
    }

    // Test 2
    @Test
    void createPlan_returns400_whenFarmerIdMissing() throws Exception {
        mockMvc.perform(post("/agriLink/cropSeason/createPlan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"holdingId":1,"cropId":1,"planSeason":"Kharif","planYear":2026,
                                 "sowingDate":"2026-06-15","expectedHarvestDate":"2026-10-15",
                                 "areaPlanted":3.5}"""))
                .andExpect(status().isBadRequest());
    }

    // Test 3
    @Test
    void createPlan_returns409_whenDuplicateExists() throws Exception {
        when(cropPlanService.createPlan(any()))
                .thenThrow(new ConflictException("A crop plan already exists"));
        mockMvc.perform(post("/agriLink/cropSeason/createPlan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"farmerId":1,"holdingId":1,"cropId":1,"planSeason":"Kharif",
                                 "planYear":2026,"sowingDate":"2026-06-15",
                                 "expectedHarvestDate":"2026-10-15","areaPlanted":3.5}"""))
                .andExpect(status().isConflict());
    }

    // Test 4
    @Test
    void fetchPlans_returns200_withPlanList() throws Exception {
        when(cropPlanService.fetchPlans()).thenReturn(List.of(sample));
        mockMvc.perform(get("/agriLink/cropSeason/fetchPlans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].planId").value(1));
    }

    // Test 5
    @Test
    void fetchPlans_returns404_whenEmpty() throws Exception {
        when(cropPlanService.fetchPlans())
                .thenThrow(new ResourceNotFoundException("No plans found"));
        mockMvc.perform(get("/agriLink/cropSeason/fetchPlans"))
                .andExpect(status().isNotFound());
    }

    // Test 6
    @Test
    void fetchPlanById_returns200() throws Exception {
        when(cropPlanService.fetchPlanById(1)).thenReturn(sample);
        mockMvc.perform(get("/agriLink/cropSeason/fetchPlanById/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cropName").value("Rice"));
    }

    // Test 7
    @Test
    void fetchPlanById_returns404_whenNotFound() throws Exception {
        when(cropPlanService.fetchPlanById(99))
                .thenThrow(new ResourceNotFoundException("Plan not found"));
        mockMvc.perform(get("/agriLink/cropSeason/fetchPlanById/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Plan not found"));
    }

    // Test 8
    @Test
    void updatePlan_returns200_withMessage() throws Exception {
        when(cropPlanService.updatePlan(eq(1), any())).thenReturn(sample);
        mockMvc.perform(put("/agriLink/cropSeason/updatePlan/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sowingDate\":\"2026-06-20\",\"areaPlanted\":4.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Crop plan updated successfully"));
    }

    // Test 9
    @Test
    void updatePlanStatus_returns200_withMessage() throws Exception {
        doNothing().when(cropPlanService).updatePlanStatus(eq(1), any());
        mockMvc.perform(put("/agriLink/cropSeason/updatePlanStatus/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planStatus\":\"So\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Plan status updated successfully"));
    }

    // Test 10
    @Test
    void updatePlanStatus_returns409_whenInvalidTransition() throws Exception {
        doThrow(new ConflictException("Transition not allowed"))
                .when(cropPlanService).updatePlanStatus(eq(1), any());
        mockMvc.perform(put("/agriLink/cropSeason/updatePlanStatus/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planStatus\":\"Gr\"}"))
                .andExpect(status().isConflict());
    }

    // Test 11
    @Test
    void approvePlan_returns200_withMessage() throws Exception {
        doNothing().when(cropPlanService).approvePlan(eq(1), any());
        mockMvc.perform(put("/agriLink/cropSeason/approvePlan/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approvedBy\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Plan approved successfully"));
    }

    // Test 12
    @Test
    void deletePlan_returns200_withMessage() throws Exception {
        doNothing().when(cropPlanService).deletePlan(1);
        mockMvc.perform(delete("/agriLink/cropSeason/deletePlan/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Crop plan deleted successfully"));
    }
}
