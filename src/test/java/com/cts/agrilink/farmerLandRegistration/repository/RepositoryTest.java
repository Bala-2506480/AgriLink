package com.cts.agrilink.farmerLandRegistration.repository;

import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;
import com.cts.agrilink.farmerLandRegistration.model.LandHolding;
import com.cts.agrilink.farmerLandRegistration.model.Role;
import com.cts.agrilink.farmerLandRegistration.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application.properties")
class RepositoryTest {

    @Autowired UserRepository userRepository;
    @Autowired FarmerProfileRepository farmerProfileRepository;
    @Autowired LandHoldingRepository landHoldingRepository;

    private User savedUser;
    private FarmerProfile savedFarmer;
    private LandHolding savedHolding;

    @BeforeEach
    void setUp() {
        landHoldingRepository.deleteAll();
        farmerProfileRepository.deleteAll();
        userRepository.deleteAll();

        savedUser = userRepository.save(User.builder()
                .name("Ravi Kumar").email("ravi@example.com").phone("9876543210")
                .passwordHash("hash123").status(User.UserStatus.Active)
                .role(Role.FARMER).build());

        savedFarmer = farmerProfileRepository.save(FarmerProfile.builder()
                .user(savedUser).name("Ravi Kumar")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(FarmerProfile.Gender.Male).nationalIdNumber("NID001")
                .village("Keelapavoor").district("Tirunelveli").state("Tamil Nadu")
                .phone("9876543210").bankAccountNumber("ACC001")
                .status(FarmerProfile.FarmerStatus.Active).build());

        savedHolding = landHoldingRepository.save(LandHolding.builder()
                .farmer(savedFarmer).surveyNumber("SRV-001")
                .areaAcres(new BigDecimal("2.5000"))
                .soilType(LandHolding.SoilType.Loam)
                .irrigationSource(LandHolding.IrrigationSource.Borewell)
                .ownershipType(LandHolding.OwnershipType.Owned)
                .status(LandHolding.LandStatus.Active).build());
    }

    @Test void user_FindByEmail_Found() { assertTrue(userRepository.findByEmail("ravi@example.com").isPresent()); }
    @Test void user_FindByEmail_NotFound() { assertFalse(userRepository.findByEmail("none@x.com").isPresent()); }
    @Test void user_ExistsByEmail_TrueAndFalse() { assertTrue(userRepository.existsByEmail("ravi@example.com")); assertFalse(userRepository.existsByEmail("none@x.com")); }
    @Test void user_FindById_Found() { assertTrue(userRepository.findById(savedUser.getUserId()).isPresent()); }
    @Test void user_Delete_RemovesAfterUnlinking() { landHoldingRepository.delete(savedHolding); farmerProfileRepository.delete(savedFarmer); userRepository.delete(savedUser); assertFalse(userRepository.existsByEmail("ravi@example.com")); }
    @Test void farmer_ExistsByNationalId_TrueAndFalse() { assertTrue(farmerProfileRepository.existsByNationalIdNumber("NID001")); assertFalse(farmerProfileRepository.existsByNationalIdNumber("NID999")); }
    @Test void farmer_FindByUserId_Found() { assertFalse(farmerProfileRepository.findByUser_UserId(savedUser.getUserId()).isEmpty()); }
    @Test void farmer_FindByDistrict_FoundAndNotFound() { assertFalse(farmerProfileRepository.findByDistrict("Tirunelveli").isEmpty()); assertTrue(farmerProfileRepository.findByDistrict("Unknown").isEmpty()); }
    @Test void farmer_FindByStatus_ActiveFound_InactiveEmpty() { assertFalse(farmerProfileRepository.findByStatus(FarmerProfile.FarmerStatus.Active).isEmpty()); assertTrue(farmerProfileRepository.findByStatus(FarmerProfile.FarmerStatus.Inactive).isEmpty()); }
    @Test void landHolding_ExistsBySurveyNumber_TrueAndFalse() { assertTrue(landHoldingRepository.existsBySurveyNumber("SRV-001")); assertFalse(landHoldingRepository.existsBySurveyNumber("SRV-999")); }
    @Test void landHolding_FindByFarmerId_FoundAndNotFound() { assertFalse(landHoldingRepository.findByFarmer_FarmerId(savedFarmer.getFarmerId()).isEmpty()); assertTrue(landHoldingRepository.findByFarmer_FarmerId(9999L).isEmpty()); }
    @Test void landHolding_FindByStatus_ActiveFound_DisputedEmpty() { assertFalse(landHoldingRepository.findByStatus(LandHolding.LandStatus.Active).isEmpty()); assertTrue(landHoldingRepository.findByStatus(LandHolding.LandStatus.Disputed).isEmpty()); }
    @Test void landHolding_ExistsByFarmerId_TrueAndFalse() { assertTrue(landHoldingRepository.existsByFarmer_FarmerId(savedFarmer.getFarmerId())); assertFalse(landHoldingRepository.existsByFarmer_FarmerId(9999L)); }
}