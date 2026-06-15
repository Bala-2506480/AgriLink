package com.cts.agrilink.farmerLandRegistration.serviceImpl;

import com.cts.agrilink.farmerLandRegistration.dto.ApiResponseDTO;
import com.cts.agrilink.farmerLandRegistration.dto.FarmerRequestDTO;
import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;
import com.cts.agrilink.farmerLandRegistration.model.User;
import com.cts.agrilink.farmerLandRegistration.repository.FarmerProfileRepository;
import com.cts.agrilink.farmerLandRegistration.repository.LandHoldingRepository;
import com.cts.agrilink.farmerLandRegistration.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmerServiceImplTest {

    @Mock FarmerProfileRepository farmerProfileRepository;
    @Mock UserRepository userRepository;
    @Mock LandHoldingRepository landHoldingRepository;
    @InjectMocks FarmerServiceImpl farmerService;

    private User mockUser;
    private FarmerProfile mockFarmer;
    private FarmerRequestDTO mockDTO;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().userId(1L).name("Ravi").email("r@e.com")
                .status(User.UserStatus.Active).build();

        mockFarmer = FarmerProfile.builder().farmerId(1L).user(mockUser)
                .name("Ravi Kumar").dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(FarmerProfile.Gender.Male).nationalIdNumber("NID001")
                .village("Keelapavoor").district("Tirunelveli").state("Tamil Nadu")
                .phone("9876543210").bankAccountNumber("ACC001")
                .status(FarmerProfile.FarmerStatus.Active).build();

        mockDTO = new FarmerRequestDTO();
        mockDTO.setUserId(1L); mockDTO.setName("Ravi Kumar");
        mockDTO.setDateOfBirth("1990-05-15"); mockDTO.setGender("Male");
        mockDTO.setNationalIdNumber("NID001"); mockDTO.setVillage("Keelapavoor");
        mockDTO.setDistrict("Tirunelveli"); mockDTO.setState("Tamil Nadu");
        mockDTO.setPhone("9876543210"); mockDTO.setStatus("Active");
    }

    @Test
    void createFarmer_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(farmerProfileRepository.existsByNationalIdNumber("NID001")).thenReturn(false);
        when(farmerProfileRepository.save(any())).thenReturn(mockFarmer);
        assertNotNull(farmerService.createFarmer(mockDTO));
    }

    @Test
    void createFarmer_InvalidUserId_ThrowsBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> farmerService.createFarmer(mockDTO)).getStatusCode().value());
    }

    @Test
    void createFarmer_DuplicateNationalId_ThrowsConflict() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(farmerProfileRepository.existsByNationalIdNumber("NID001")).thenReturn(true);
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> farmerService.createFarmer(mockDTO)).getStatusCode().value());
    }

    @Test
    void fetchAllFarmers_Success() {
        when(farmerProfileRepository.findAll()).thenReturn(List.of(mockFarmer));
        assertEquals(1, farmerService.fetchAllFarmers().size());
    }

    @Test
    void fetchAllFarmers_Empty_ThrowsNotFound() {
        when(farmerProfileRepository.findAll()).thenReturn(Collections.emptyList());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> farmerService.fetchAllFarmers()).getStatusCode().value());
    }

    @Test
    void fetchFarmerById_Found() {
        when(farmerProfileRepository.findById(1L)).thenReturn(Optional.of(mockFarmer));
        assertNotNull(farmerService.fetchFarmerById(1L));
    }

    @Test
    void fetchFarmerById_NotFound_Throws404() {
        when(farmerProfileRepository.findById(99L)).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> farmerService.fetchFarmerById(99L)).getStatusCode().value());
    }

    @Test
    void fetchFarmersByUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(farmerProfileRepository.findByUser_UserId(1L)).thenReturn(List.of(mockFarmer));
        assertEquals(1, farmerService.fetchFarmersByUser(1L).size());
    }

    @Test
    void fetchFarmersByDistrict_Success() {
        when(farmerProfileRepository.findByDistrict("Tirunelveli")).thenReturn(List.of(mockFarmer));
        assertEquals(1, farmerService.fetchFarmersByDistrict("Tirunelveli").size());
    }

    @Test
    void fetchFarmersByDistrict_Empty_Throws404() {
        when(farmerProfileRepository.findByDistrict("Unknown")).thenReturn(Collections.emptyList());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> farmerService.fetchFarmersByDistrict("Unknown")).getStatusCode().value());
    }

    @Test
    void fetchFarmersByStatus_Success() {
        when(farmerProfileRepository.findByStatus(FarmerProfile.FarmerStatus.Active))
                .thenReturn(List.of(mockFarmer));
        assertEquals(1, farmerService.fetchFarmersByStatus("Active").size());
    }

    @Test
    void fetchFarmersByStatus_InvalidStatus_Throws400() {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> farmerService.fetchFarmersByStatus("INVALID")).getStatusCode().value());
    }

    @Test
    void updateFarmer_Success() {
        when(farmerProfileRepository.findById(1L)).thenReturn(Optional.of(mockFarmer));
        when(farmerProfileRepository.save(any())).thenReturn(mockFarmer);
        mockDTO.setStatus("Verified");
        assertNotNull(farmerService.updateFarmer(1L, mockDTO));
    }

    @Test
    void updateFarmer_NotFound_Throws404() {
        when(farmerProfileRepository.findById(99L)).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> farmerService.updateFarmer(99L, mockDTO)).getStatusCode().value());
    }

    @Test
    void deleteFarmer_Success() {
        when(farmerProfileRepository.findById(1L)).thenReturn(Optional.of(mockFarmer));
        when(landHoldingRepository.existsByFarmer_FarmerId(1L)).thenReturn(false);
        assertEquals("Farmer deleted successfully", farmerService.deleteFarmer(1L).getMessage());
    }

    @Test
    void deleteFarmer_LandHoldingsExist_Throws409() {
        when(farmerProfileRepository.findById(1L)).thenReturn(Optional.of(mockFarmer));
        when(landHoldingRepository.existsByFarmer_FarmerId(1L)).thenReturn(true);
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> farmerService.deleteFarmer(1L)).getStatusCode().value());
    }

    @Test
    void softDeleteFarmer_Success() {
        when(farmerProfileRepository.findById(1L)).thenReturn(Optional.of(mockFarmer));
        when(farmerProfileRepository.save(any())).thenReturn(mockFarmer);
        ApiResponseDTO result = farmerService.softDeleteFarmer(1L);
        assertEquals("Farmer deactivated successfully", result.getMessage());
        assertEquals(FarmerProfile.FarmerStatus.Inactive, mockFarmer.getStatus());
    }
}
