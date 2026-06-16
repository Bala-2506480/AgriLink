package com.cts.agrilink.controller;

import com.cts.agrilink.dto.AgrilinkDto.*;
import com.cts.agrilink.exception.ConflictException;
import com.cts.agrilink.exception.GlobalExceptionHandler;
import com.cts.agrilink.exception.ResourceNotFoundException;
import com.cts.agrilink.model.GrowthObservation;
import com.cts.agrilink.service.GrowthObservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GrowthObservationControllerTest {

    @Mock
    private GrowthObservationService growthObservationService;

    @InjectMocks
    private GrowthObservationController growthObservationController;

    private MockMvc mockMvc;
    private ObservationResponse sample;

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
                .standaloneSetup(growthObservationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sample = new ObservationResponse();
        set(sample, "observationId",    1);
        set(sample, "planId",           1);
        set(sample, "officerId",        2);
        set(sample, "observationDate",  LocalDate.of(2026, 7, 5));
        set(sample, "growthStage",      GrowthObservation.GrowthStage.Germination);
        set(sample, "pestOrDiseaseFlag", false);
        set(sample, "fieldRemarks",     "Healthy germination.");
        set(sample, "createdAt",        LocalDateTime.of(2026, 7, 5, 10, 0));
    }

    // Test 1
    @Test
    void createObservation_returns201_withMessageAndData() throws Exception {
        when(growthObservationService.createObservation(any())).thenReturn(sample);
        mockMvc.perform(post("/agriLink/cropSeason/createObservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"planId":1,"officerId":2,"observationDate":"2026-07-05",
                                 "growthStage":"Germination","pestOrDiseaseFlag":false}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Observation recorded successfully"));
    }

    // Test 2
    @Test
    void createObservation_returns400_whenPlanIdMissing() throws Exception {
        mockMvc.perform(post("/agriLink/cropSeason/createObservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"officerId":2,"observationDate":"2026-07-05","growthStage":"Germination"}"""))
                .andExpect(status().isBadRequest());
    }

    // Test 3
    @Test
    void createObservation_returns409_whenPlanIsHarvested() throws Exception {
        when(growthObservationService.createObservation(any()))
                .thenThrow(new ConflictException("Cannot record observation for a Ha plan"));
        mockMvc.perform(post("/agriLink/cropSeason/createObservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"planId":1,"officerId":2,"observationDate":"2026-07-05",
                                 "growthStage":"Germination"}"""))
                .andExpect(status().isConflict());
    }

    // Test 4
    @Test
    void fetchObservations_returns200_withList() throws Exception {
        when(growthObservationService.fetchObservations()).thenReturn(List.of(sample));
        mockMvc.perform(get("/agriLink/cropSeason/fetchObservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].observationId").value(1));
    }

    // Test 5
    @Test
    void fetchObservations_returns404_whenEmpty() throws Exception {
        when(growthObservationService.fetchObservations())
                .thenThrow(new ResourceNotFoundException("No observations found"));
        mockMvc.perform(get("/agriLink/cropSeason/fetchObservations"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No observations found"));
    }

    // Test 6
    @Test
    void fetchObservationById_returns200() throws Exception {
        when(growthObservationService.fetchObservationById(1)).thenReturn(sample);
        mockMvc.perform(get("/agriLink/cropSeason/fetchObservationById/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.officerId").value(2))
                .andExpect(jsonPath("$.growthStage").value("Germination"));
    }

    // Test 7
    @Test
    void fetchObservationById_returns404_whenNotFound() throws Exception {
        when(growthObservationService.fetchObservationById(99))
                .thenThrow(new ResourceNotFoundException("Observation not found"));
        mockMvc.perform(get("/agriLink/cropSeason/fetchObservationById/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Observation not found"));
    }

    // Test 8
    @Test
    void fetchByPlan_returns200() throws Exception {
        when(growthObservationService.fetchByPlan(1)).thenReturn(List.of(sample));
        mockMvc.perform(get("/agriLink/cropSeason/fetchByPlan/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].planId").value(1));
    }

    // Test 9
    @Test
    void updateObservation_returns200_withMessage() throws Exception {
        when(growthObservationService.updateObservation(eq(1), any())).thenReturn(sample);
        mockMvc.perform(put("/agriLink/cropSeason/updateObservation/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"growthStage\":\"Vegetative\",\"pestOrDiseaseFlag\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Growth observation updated successfully"));
    }

    // Test 10
    @Test
    void updatePestFlag_returns200_withMessage() throws Exception {
        doNothing().when(growthObservationService).updatePestFlag(eq(1), any());
        mockMvc.perform(put("/agriLink/cropSeason/updatePestFlag/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pestOrDiseaseFlag\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pest flag updated successfully"));
    }

    // Test 11
    @Test
    void updatePestFlag_returns404_whenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Observation not found"))
                .when(growthObservationService).updatePestFlag(eq(99), any());
        mockMvc.perform(put("/agriLink/cropSeason/updatePestFlag/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pestOrDiseaseFlag\":true}"))
                .andExpect(status().isNotFound());
    }

    // Test 12
    @Test
    void deleteObservation_returns200_withMessage() throws Exception {
        doNothing().when(growthObservationService).deleteObservation(1);
        mockMvc.perform(delete("/agriLink/cropSeason/deleteObservation/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Observation deleted successfully"));
    }
}
