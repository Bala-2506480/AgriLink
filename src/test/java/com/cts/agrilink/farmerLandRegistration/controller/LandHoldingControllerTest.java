package com.cts.agrilink.farmerLandRegistration.controller;

import com.cts.agrilink.farmerLandRegistration.dto.ApiResponseDTO;
import com.cts.agrilink.farmerLandRegistration.dto.LandHoldingRequestDTO;
import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;
import com.cts.agrilink.farmerLandRegistration.model.LandHolding;
import com.cts.agrilink.farmerLandRegistration.model.User;
import com.cts.agrilink.farmerLandRegistration.service.LandHoldingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LandHoldingController.class)
class LandHoldingControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean LandHoldingService landHoldingService;
    @Autowired ObjectMapper objectMapper;

    private LandHolding mockHolding;
    private LandHoldingRequestDTO mockDTO;

    @BeforeEach
    void setUp() {
        User user = User.builder().userId(1L).name("Ravi").email("r@e.com")
                .status(User.UserStatus.Active).build();
        FarmerProfile farmer = FarmerProfile.builder().farmerId(1L).user(user).name("Ravi")
                .dateOfBirth(LocalDate.of(1990, 5, 15)).gender(FarmerProfile.Gender.Male)
                .nationalIdNumber("NID001").village("V").district("D").state("S")
                .phone("9876543210").status(FarmerProfile.FarmerStatus.Active).build();
        mockHolding = LandHolding.builder().holdingId(1L).farmer(farmer).surveyNumber("SRV-001")
                .areaAcres(new BigDecimal("2.5000")).soilType(LandHolding.SoilType.Loam)
                .irrigationSource(LandHolding.IrrigationSource.Borewell)
                .ownershipType(LandHolding.OwnershipType.Owned)
                .status(LandHolding.LandStatus.Active).build();

        mockDTO = new LandHoldingRequestDTO();
        mockDTO.setFarmerId(1L); mockDTO.setSurveyNumber("SRV-001");
        mockDTO.setAreaAcres(new BigDecimal("2.5000")); mockDTO.setSoilType("Loam");
        mockDTO.setIrrigationSource("Borewell"); mockDTO.setOwnershipType("Owned");
        mockDTO.setStatus("Active");
    }

    @Test
    void createLandHolding_Returns201() throws Exception {
        when(landHoldingService.createLandHolding(any())).thenReturn(mockHolding);
        mockMvc.perform(post("/farmerLandRegistration/landHolding/createLandHolding")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(mockDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Land holding created successfully"));
    }

    @Test
    void createLandHolding_MissingSurveyNumber_Returns400() throws Exception {
        mockDTO.setSurveyNumber("");
        mockMvc.perform(post("/farmerLandRegistration/landHolding/createLandHolding")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(mockDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createLandHolding_DuplicateSurveyNumber_Returns409() throws Exception {
        when(landHoldingService.createLandHolding(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Survey number already exists"));
        mockMvc.perform(post("/farmerLandRegistration/landHolding/createLandHolding")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(mockDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    void fetchAllLandHoldings_Returns200() throws Exception {
        when(landHoldingService.fetchAllLandHoldings()).thenReturn(List.of(mockHolding));
        mockMvc.perform(get("/farmerLandRegistration/landHolding/fetchLandHoldings"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].surveyNumber").value("SRV-001"));
    }

    @Test
    void fetchLandHoldingById_Returns200() throws Exception {
        when(landHoldingService.fetchLandHoldingById(1L)).thenReturn(mockHolding);
        mockMvc.perform(get("/farmerLandRegistration/landHolding/fetchLandHoldingById/1"))
                .andExpect(status().isOk());
    }

    @Test
    void fetchLandHoldingsByFarmer_Returns200() throws Exception {
        when(landHoldingService.fetchLandHoldingsByFarmer(1L)).thenReturn(List.of(mockHolding));
        mockMvc.perform(get("/farmerLandRegistration/landHolding/fetchLandHoldingsByFarmer/1"))
                .andExpect(status().isOk());
    }

    @Test
    void fetchLandHoldingsByStatus_Returns200() throws Exception {
        when(landHoldingService.fetchLandHoldingsByStatus("Active")).thenReturn(List.of(mockHolding));
        mockMvc.perform(get("/farmerLandRegistration/landHolding/fetchLandHoldingsByStatus/Active"))
                .andExpect(status().isOk());
    }

    @Test
    void updateLandHolding_Returns200() throws Exception {
        when(landHoldingService.updateLandHolding(eq(1L), any())).thenReturn(mockHolding);
        mockMvc.perform(put("/farmerLandRegistration/landHolding/updateLandHolding/1")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(mockDTO)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Land holding updated successfully"));
    }

    @Test
    void deleteLandHolding_Returns204() throws Exception {
        when(landHoldingService.deleteLandHolding(1L)).thenReturn(new ApiResponseDTO("Land holding deleted successfully"));
        mockMvc.perform(delete("/farmerLandRegistration/landHolding/deleteLandHolding/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void softDeleteLandHolding_Returns200() throws Exception {
        when(landHoldingService.softDeleteLandHolding(1L)).thenReturn(new ApiResponseDTO("Land holding deactivated successfully"));
        mockMvc.perform(delete("/farmerLandRegistration/landHolding/deleteLandHolding/1/soft"))
                .andExpect(status().isOk());
    }
}
