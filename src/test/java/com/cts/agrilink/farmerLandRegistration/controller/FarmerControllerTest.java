package com.cts.agrilink.farmerLandRegistration.controller;

import com.cts.agrilink.farmerLandRegistration.dto.ApiResponseDTO;
import com.cts.agrilink.farmerLandRegistration.dto.FarmerRequestDTO;
import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;
import com.cts.agrilink.farmerLandRegistration.model.Role;
import com.cts.agrilink.farmerLandRegistration.model.User;
import com.cts.agrilink.farmerLandRegistration.security.CustomUserDetailsService;
import com.cts.agrilink.farmerLandRegistration.security.JwtUtil;
import com.cts.agrilink.farmerLandRegistration.service.FarmerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FarmerController.class)
class FarmerControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean FarmerService farmerService;
    @MockBean JwtUtil jwtUtil;
    @MockBean CustomUserDetailsService customUserDetailsService; // ADDED
    @Autowired ObjectMapper objectMapper;

    private FarmerProfile mockFarmer;
    private FarmerRequestDTO mockDTO;

    @BeforeEach
    void setUp() {
        User mockUser = User.builder().userId(1L).name("Ravi").email("r@e.com")
                .status(User.UserStatus.Active).role(Role.FARMER).build();
        mockFarmer = FarmerProfile.builder().farmerId(1L).user(mockUser).name("Ravi Kumar")
                .dateOfBirth(LocalDate.of(1990, 5, 15)).gender(FarmerProfile.Gender.Male)
                .nationalIdNumber("NID001").village("Keelapavoor").district("Tirunelveli")
                .state("Tamil Nadu").phone("9876543210")
                .status(FarmerProfile.FarmerStatus.Active).build();

        mockDTO = new FarmerRequestDTO();
        mockDTO.setUserId(1L); mockDTO.setName("Ravi Kumar"); mockDTO.setDateOfBirth("1990-05-15");
        mockDTO.setGender("Male"); mockDTO.setNationalIdNumber("NID001");
        mockDTO.setVillage("Keelapavoor"); mockDTO.setDistrict("Tirunelveli");
        mockDTO.setState("Tamil Nadu"); mockDTO.setPhone("9876543210"); mockDTO.setStatus("Active");
    }

    @Test @WithMockUser(roles = "ADMIN")
    void createFarmer_Returns201() throws Exception {
        when(farmerService.createFarmer(any())).thenReturn(mockFarmer);
        mockMvc.perform(post("/farmerLandRegistration/farmer/createFarmer").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(mockDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Farmer profile created successfully"));
    }

    @Test @WithMockUser(roles = "ADMIN")
    void createFarmer_MissingName_Returns400() throws Exception {
        mockDTO.setName("");
        mockMvc.perform(post("/farmerLandRegistration/farmer/createFarmer").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(mockDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test @WithMockUser(roles = "ADMIN")
    void createFarmer_DuplicateNationalId_Returns409() throws Exception {
        when(farmerService.createFarmer(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "National ID already registered"));
        mockMvc.perform(post("/farmerLandRegistration/farmer/createFarmer").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(mockDTO)))
                .andExpect(status().isConflict());
    }

    @Test @WithMockUser(roles = "ADMIN")
    void fetchAllFarmers_Returns200() throws Exception {
        when(farmerService.fetchAllFarmers()).thenReturn(List.of(mockFarmer));
        mockMvc.perform(get("/farmerLandRegistration/farmer/fetchFarmers"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Ravi Kumar"));
    }

    @Test @WithMockUser(roles = "ADMIN")
    void fetchAllFarmers_Empty_Returns404() throws Exception {
        when(farmerService.fetchAllFarmers())
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No farmers found"));
        mockMvc.perform(get("/farmerLandRegistration/farmer/fetchFarmers"))
                .andExpect(status().isNotFound());
    }

    @Test @WithMockUser(roles = "FARMER")
    void fetchFarmerById_Returns200() throws Exception {
        when(farmerService.fetchFarmerById(1L)).thenReturn(mockFarmer);
        mockMvc.perform(get("/farmerLandRegistration/farmer/fetchFarmerById/1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nationalIdNumber").value("NID001"));
    }

    @Test @WithMockUser(roles = "FARMER")
    void fetchFarmersByUser_Returns200() throws Exception {
        when(farmerService.fetchFarmersByUser(1L)).thenReturn(List.of(mockFarmer));
        mockMvc.perform(get("/farmerLandRegistration/farmer/fetchFarmersByUser/1"))
                .andExpect(status().isOk());
    }

    @Test @WithMockUser(roles = "FARMER")
    void fetchFarmersByDistrict_Returns200() throws Exception {
        when(farmerService.fetchFarmersByDistrict("Tirunelveli")).thenReturn(List.of(mockFarmer));
        mockMvc.perform(get("/farmerLandRegistration/farmer/fetchFarmersByDistrict/Tirunelveli"))
                .andExpect(status().isOk());
    }

    @Test @WithMockUser(roles = "FARMER")
    void fetchFarmersByStatus_Returns200() throws Exception {
        when(farmerService.fetchFarmersByStatus("Active")).thenReturn(List.of(mockFarmer));
        mockMvc.perform(get("/farmerLandRegistration/farmer/fetchFarmersByStatus/Active"))
                .andExpect(status().isOk());
    }

    @Test @WithMockUser(roles = "ADMIN")
    void updateFarmer_Returns200() throws Exception {
        when(farmerService.updateFarmer(eq(1L), any())).thenReturn(mockFarmer);
        mockMvc.perform(put("/farmerLandRegistration/farmer/updateFarmer/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(mockDTO)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Farmer updated successfully"));
    }

    @Test @WithMockUser(roles = "ADMIN")
    void deleteFarmer_Returns204() throws Exception {
        when(farmerService.deleteFarmer(1L)).thenReturn(new ApiResponseDTO("Farmer deleted successfully"));
        mockMvc.perform(delete("/farmerLandRegistration/farmer/deleteFarmer/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test @WithMockUser(roles = "ADMIN")
    void deleteFarmer_LandHoldingsExist_Returns409() throws Exception {
        when(farmerService.deleteFarmer(1L))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Farmer cannot be deleted, land holdings exist"));
        mockMvc.perform(delete("/farmerLandRegistration/farmer/deleteFarmer/1").with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test @WithMockUser(roles = "ADMIN")
    void softDeleteFarmer_Returns200() throws Exception {
        when(farmerService.softDeleteFarmer(1L)).thenReturn(new ApiResponseDTO("Farmer deactivated successfully"));
        mockMvc.perform(delete("/farmerLandRegistration/farmer/deleteFarmer/1/soft").with(csrf()))
                .andExpect(status().isOk());
    }
}