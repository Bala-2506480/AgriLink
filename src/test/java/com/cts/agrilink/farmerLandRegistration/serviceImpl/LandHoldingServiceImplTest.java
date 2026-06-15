package com.cts.agrilink.farmerLandRegistration.serviceImpl;

import com.cts.agrilink.farmerLandRegistration.dto.ApiResponseDTO;
import com.cts.agrilink.farmerLandRegistration.dto.LandHoldingRequestDTO;
import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;
import com.cts.agrilink.farmerLandRegistration.model.LandHolding;
import com.cts.agrilink.farmerLandRegistration.model.User;
import com.cts.agrilink.farmerLandRegistration.repository.FarmerProfileRepository;
import com.cts.agrilink.farmerLandRegistration.repository.LandHoldingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LandHoldingServiceImplTest {

    @Mock LandHoldingRepository landHoldingRepository;
    @Mock FarmerProfileRepository farmerProfileRepository;
    @InjectMocks LandHoldingServiceImpl landHoldingService;

    private FarmerProfile mockFarmer;
    private LandHolding mockHolding;
    private LandHoldingRequestDTO mockDTO;

    @BeforeEach
    void setUp() {
        User mockUser = User.builder().userId(1L).name("Ravi").email("r@e.com")
                .status(User.UserStatus.Active).build();

        mockFarmer = FarmerProfile.builder().farmerId(1L).user(mockUser).name("Ravi")
                .dateOfBirth(LocalDate.of(1990, 5, 15)).gender(FarmerProfile.Gender.Male)
                .nationalIdNumber("NID001").village("V").district("D").state("S")
                .phone("9876543210").status(FarmerProfile.FarmerStatus.Active).build();

        mockHolding = LandHolding.builder().holdingId(1L).farmer(mockFarmer)
                .surveyNumber("SRV-001").areaAcres(new BigDecimal("2.5000"))
                .soilType(LandHolding.SoilType.Loam)
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
    void createLandHolding_Success() {
        when(farmerProfileRepository.findById(1L)).thenReturn(Optional.of(mockFarmer));
        when(landHoldingRepository.existsBySurveyNumber("SRV-001")).thenReturn(false);
        when(landHoldingRepository.save(any())).thenReturn(mockHolding);
        assertNotNull(landHoldingService.createLandHolding(mockDTO));
    }

    @Test
    void createLandHolding_InvalidFarmerId_Throws400() {
        when(farmerProfileRepository.findById(1L)).thenReturn(Optional.empty());
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> landHoldingService.createLandHolding(mockDTO)).getStatusCode().value());
    }

    @Test
    void createLandHolding_DuplicateSurveyNumber_Throws409() {
        when(farmerProfileRepository.findById(1L)).thenReturn(Optional.of(mockFarmer));
        when(landHoldingRepository.existsBySurveyNumber("SRV-001")).thenReturn(true);
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> landHoldingService.createLandHolding(mockDTO)).getStatusCode().value());
    }

    @Test
    void fetchAllLandHoldings_Success() {
        when(landHoldingRepository.findAll()).thenReturn(List.of(mockHolding));
        assertEquals(1, landHoldingService.fetchAllLandHoldings().size());
    }

    @Test
    void fetchAllLandHoldings_Empty_Throws404() {
        when(landHoldingRepository.findAll()).thenReturn(Collections.emptyList());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> landHoldingService.fetchAllLandHoldings()).getStatusCode().value());
    }

    @Test
    void fetchLandHoldingById_Found() {
        when(landHoldingRepository.findById(1L)).thenReturn(Optional.of(mockHolding));
        assertNotNull(landHoldingService.fetchLandHoldingById(1L));
    }

    @Test
    void fetchLandHoldingById_NotFound_Throws404() {
        when(landHoldingRepository.findById(99L)).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> landHoldingService.fetchLandHoldingById(99L)).getStatusCode().value());
    }

    @Test
    void fetchLandHoldingsByFarmer_Success() {
        when(farmerProfileRepository.findById(1L)).thenReturn(Optional.of(mockFarmer));
        when(landHoldingRepository.findByFarmer_FarmerId(1L)).thenReturn(List.of(mockHolding));
        assertEquals(1, landHoldingService.fetchLandHoldingsByFarmer(1L).size());
    }

    @Test
    void fetchLandHoldingsByStatus_Success() {
        when(landHoldingRepository.findByStatus(LandHolding.LandStatus.Active))
                .thenReturn(List.of(mockHolding));
        assertEquals(1, landHoldingService.fetchLandHoldingsByStatus("Active").size());
    }

    @Test
    void fetchLandHoldingsByStatus_InvalidStatus_Throws400() {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> landHoldingService.fetchLandHoldingsByStatus("INVALID")).getStatusCode().value());
    }

    @Test
    void updateLandHolding_Success() {
        when(landHoldingRepository.findById(1L)).thenReturn(Optional.of(mockHolding));
        when(landHoldingRepository.save(any())).thenReturn(mockHolding);
        mockDTO.setSoilType("Clay"); mockDTO.setStatus("Active");
        assertNotNull(landHoldingService.updateLandHolding(1L, mockDTO));
    }

    @Test
    void updateLandHolding_NotFound_Throws404() {
        when(landHoldingRepository.findById(99L)).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> landHoldingService.updateLandHolding(99L, mockDTO)).getStatusCode().value());
    }

    @Test
    void deleteLandHolding_Success() {
        when(landHoldingRepository.findById(1L)).thenReturn(Optional.of(mockHolding));
        ApiResponseDTO result = landHoldingService.deleteLandHolding(1L);
        assertEquals("Land holding deleted successfully", result.getMessage());
        verify(landHoldingRepository).delete(mockHolding);
    }

    @Test
    void softDeleteLandHolding_Success() {
        when(landHoldingRepository.findById(1L)).thenReturn(Optional.of(mockHolding));
        when(landHoldingRepository.save(any())).thenReturn(mockHolding);
        ApiResponseDTO result = landHoldingService.softDeleteLandHolding(1L);
        assertEquals("Land holding deactivated successfully", result.getMessage());
        assertEquals(LandHolding.LandStatus.Disputed, mockHolding.getStatus());
    }
}
