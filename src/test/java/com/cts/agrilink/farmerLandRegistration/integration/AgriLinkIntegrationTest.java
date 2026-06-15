package com.cts.agrilink.farmerLandRegistration.integration;

import com.cts.agrilink.farmerLandRegistration.dto.FarmerRequestDTO;
import com.cts.agrilink.farmerLandRegistration.dto.LandHoldingRequestDTO;
import com.cts.agrilink.farmerLandRegistration.dto.UserRequestDTO;
import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;
import com.cts.agrilink.farmerLandRegistration.model.LandHolding;
import com.cts.agrilink.farmerLandRegistration.model.User;
import com.cts.agrilink.farmerLandRegistration.repository.FarmerProfileRepository;
import com.cts.agrilink.farmerLandRegistration.repository.LandHoldingRepository;
import com.cts.agrilink.farmerLandRegistration.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AgriLinkIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired FarmerProfileRepository farmerProfileRepository;
    @Autowired LandHoldingRepository landHoldingRepository;

    // Shared IDs across tests
    static Long createdUserId;
    static Long createdFarmerId;
    static Long createdHoldingId;

    @BeforeEach
    void cleanUp() {
        landHoldingRepository.deleteAll();
        farmerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ==================== USER INTEGRATION ====================

    @Test
    @Order(1)
    void createUser_ThenVerifyInDB() throws Exception {
        UserRequestDTO dto = buildUserDTO("integration_ravi@example.com");

        MvcResult result = mockMvc.perform(post("/farmerLandRegistration/createUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andReturn();

        // Verify actually saved in DB
        assertTrue(userRepository.existsByEmail("integration_ravi@example.com"));
        assertEquals(1, userRepository.findAll().size());
    }

    @Test
    @Order(2)
    void createUser_DuplicateEmail_Returns409() throws Exception {
        UserRequestDTO dto = buildUserDTO("dup@example.com");

        // First creation
        mockMvc.perform(post("/farmerLandRegistration/createUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Duplicate
        mockMvc.perform(post("/farmerLandRegistration/createUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());

        // Only 1 saved
        assertEquals(1, userRepository.findAll().size());
    }

    @Test
    @Order(3)
    void updateUser_ThenVerifyInDB() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Old Name").email("update@example.com")
                .phone("9000000000").passwordHash("hash")
                .status(User.UserStatus.Active).build());

        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("New Name");
        dto.setPhone("9111111111");
        dto.setStatus("Inactive");

        mockMvc.perform(put("/farmerLandRegistration/updateUser/" + user.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User updated successfully"));

        User updated = userRepository.findById(user.getUserId()).orElseThrow();
        assertEquals("New Name", updated.getName());
        assertEquals("9111111111", updated.getPhone());
        assertEquals(User.UserStatus.Inactive, updated.getStatus());
    }

    @Test
    @Order(4)
    void deleteUser_NoLinkedRecords_Returns204() throws Exception {
        User user = userRepository.save(User.builder()
                .name("To Delete").email("delete@example.com")
                .phone("9000000000").passwordHash("hash")
                .status(User.UserStatus.Active).build());

        mockMvc.perform(delete("/farmerLandRegistration/deleteUser/" + user.getUserId()))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.existsByEmail("delete@example.com"));
    }

    @Test
    @Order(5)
    void deleteUser_WithLinkedFarmer_Returns409() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Linked").email("linked@example.com")
                .phone("9000000000").passwordHash("hash")
                .status(User.UserStatus.Active).build());

        farmerProfileRepository.save(buildFarmerEntity(user, "NID_LINKED"));

        mockMvc.perform(delete("/farmerLandRegistration/deleteUser/" + user.getUserId()))
                .andExpect(status().isConflict());

        assertTrue(userRepository.existsByEmail("linked@example.com"));
    }

    // ==================== FARMER INTEGRATION ====================

    @Test
    @Order(6)
    void createFarmer_ThenVerifyInDB() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Ravi").email("farmer_test@example.com")
                .phone("9876543210").passwordHash("hash")
                .status(User.UserStatus.Active).build());

        FarmerRequestDTO dto = buildFarmerDTO(user.getUserId(), "NID_INT_001");

        mockMvc.perform(post("/farmerLandRegistration/farmer/createFarmer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Farmer profile created successfully"));

        assertTrue(farmerProfileRepository.existsByNationalIdNumber("NID_INT_001"));
    }

    @Test
    @Order(7)
    void createFarmer_DuplicateNationalId_Returns409() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Ravi").email("dup_farmer@example.com")
                .phone("9876543210").passwordHash("hash")
                .status(User.UserStatus.Active).build());

        farmerProfileRepository.save(buildFarmerEntity(user, "NID_DUP"));

        FarmerRequestDTO dto = buildFarmerDTO(user.getUserId(), "NID_DUP");

        mockMvc.perform(post("/farmerLandRegistration/farmer/createFarmer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(8)
    void fetchFarmersByDistrict_ReturnsCorrectData() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Ravi").email("district@example.com")
                .phone("9876543210").passwordHash("hash")
                .status(User.UserStatus.Active).build());

        farmerProfileRepository.save(buildFarmerEntity(user, "NID_DIST_001"));

        mockMvc.perform(get("/farmerLandRegistration/farmer/fetchFarmersByDistrict/Tirunelveli"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].district").value("Tirunelveli"));
    }

    @Test
    @Order(9)
    void updateFarmer_ThenVerifyInDB() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Ravi").email("update_farmer@example.com")
                .phone("9876543210").passwordHash("hash")
                .status(User.UserStatus.Active).build());

        FarmerProfile farmer = farmerProfileRepository.save(buildFarmerEntity(user, "NID_UPD"));

        FarmerRequestDTO dto = new FarmerRequestDTO();
        dto.setVillage("Palayamkottai");
        dto.setPhone("9999999999");
        dto.setStatus("Verified");

        mockMvc.perform(put("/farmerLandRegistration/farmer/updateFarmer/" + farmer.getFarmerId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Farmer updated successfully"));

        FarmerProfile updated = farmerProfileRepository.findById(farmer.getFarmerId()).orElseThrow();
        assertEquals("Palayamkottai", updated.getVillage());
        assertEquals(FarmerProfile.FarmerStatus.Verified, updated.getStatus());
    }

    @Test
    @Order(10)
    void deleteFarmer_WithLandHoldings_Returns409() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Ravi").email("del_farmer@example.com")
                .phone("9876543210").passwordHash("hash")
                .status(User.UserStatus.Active).build());

        FarmerProfile farmer = farmerProfileRepository.save(buildFarmerEntity(user, "NID_DEL"));
        landHoldingRepository.save(buildHoldingEntity(farmer, "SRV_DEL_001"));

        mockMvc.perform(delete("/farmerLandRegistration/farmer/deleteFarmer/" + farmer.getFarmerId()))
                .andExpect(status().isConflict());

        assertTrue(farmerProfileRepository.existsByNationalIdNumber("NID_DEL"));
    }

    // ==================== LAND HOLDING INTEGRATION ====================

    @Test
    @Order(11)
    void createLandHolding_ThenVerifyInDB() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Ravi").email("land@example.com")
                .phone("9876543210").passwordHash("hash")
                .status(User.UserStatus.Active).build());

        FarmerProfile farmer = farmerProfileRepository.save(buildFarmerEntity(user, "NID_LAND_001"));

        LandHoldingRequestDTO dto = buildLandHoldingDTO(farmer.getFarmerId(), "SRV_INT_001");

        mockMvc.perform(post("/farmerLandRegistration/landHolding/createLandHolding")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Land holding created successfully"));

        assertTrue(landHoldingRepository.existsBySurveyNumber("SRV_INT_001"));
    }

    @Test
    @Order(12)
    void createLandHolding_DuplicateSurveyNumber_Returns409() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Ravi").email("dup_land@example.com")
                .phone("9876543210").passwordHash("hash")
                .status(User.UserStatus.Active).build());

        FarmerProfile farmer = farmerProfileRepository.save(buildFarmerEntity(user, "NID_DUPLAND"));
        landHoldingRepository.save(buildHoldingEntity(farmer, "SRV_DUP_001"));

        LandHoldingRequestDTO dto = buildLandHoldingDTO(farmer.getFarmerId(), "SRV_DUP_001");

        mockMvc.perform(post("/farmerLandRegistration/landHolding/createLandHolding")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(13)
    void updateLandHolding_ThenVerifyInDB() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Ravi").email("upd_land@example.com")
                .phone("9876543210").passwordHash("hash")
                .status(User.UserStatus.Active).build());

        FarmerProfile farmer = farmerProfileRepository.save(buildFarmerEntity(user, "NID_UPDLAND"));
        LandHolding holding = landHoldingRepository.save(buildHoldingEntity(farmer, "SRV_UPD_001"));

        LandHoldingRequestDTO dto = new LandHoldingRequestDTO();
        dto.setAreaAcres(new BigDecimal("5.0000"));
        dto.setSoilType("Clay");
        dto.setIrrigationSource("Rain");
        dto.setOwnershipType("Leased");
        dto.setStatus("Active");

        mockMvc.perform(put("/farmerLandRegistration/landHolding/updateLandHolding/" + holding.getHoldingId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Land holding updated successfully"));

        LandHolding updated = landHoldingRepository.findById(holding.getHoldingId()).orElseThrow();
        assertEquals(new BigDecimal("5.0000"), updated.getAreaAcres());
        assertEquals(LandHolding.SoilType.Clay, updated.getSoilType());
        assertEquals(LandHolding.IrrigationSource.Rain, updated.getIrrigationSource());
    }

    @Test
    @Order(14)
    void fetchLandHoldingsByFarmer_ReturnsCorrectData() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Ravi").email("fetch_land@example.com")
                .phone("9876543210").passwordHash("hash")
                .status(User.UserStatus.Active).build());

        FarmerProfile farmer = farmerProfileRepository.save(buildFarmerEntity(user, "NID_FETCHLAND"));
        landHoldingRepository.save(buildHoldingEntity(farmer, "SRV_FETCH_001"));

        mockMvc.perform(get("/farmerLandRegistration/landHolding/fetchLandHoldingsByFarmer/" + farmer.getFarmerId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].surveyNumber").value("SRV_FETCH_001"));
    }

    @Test
    @Order(15)
    void deleteLandHolding_Returns204_AndRemovedFromDB() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Ravi").email("del_land@example.com")
                .phone("9876543210").passwordHash("hash")
                .status(User.UserStatus.Active).build());

        FarmerProfile farmer = farmerProfileRepository.save(buildFarmerEntity(user, "NID_DELLAND"));
        LandHolding holding = landHoldingRepository.save(buildHoldingEntity(farmer, "SRV_DEL_LAND"));

        mockMvc.perform(delete("/farmerLandRegistration/landHolding/deleteLandHolding/" + holding.getHoldingId()))
                .andExpect(status().isNoContent());

        assertFalse(landHoldingRepository.existsBySurveyNumber("SRV_DEL_LAND"));
    }

    // ==================== HELPERS ====================

    private UserRequestDTO buildUserDTO(String email) {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Ravi Kumar"); dto.setEmail(email);
        dto.setPhone("9876543210"); dto.setPasswordHash("hash123");
        dto.setStatus("Active");
        return dto;
    }

    private FarmerRequestDTO buildFarmerDTO(Long userId, String nationalId) {
        FarmerRequestDTO dto = new FarmerRequestDTO();
        dto.setUserId(userId); dto.setName("Ravi Kumar");
        dto.setDateOfBirth("1990-05-15"); dto.setGender("Male");
        dto.setNationalIdNumber(nationalId); dto.setVillage("Keelapavoor");
        dto.setDistrict("Tirunelveli"); dto.setState("Tamil Nadu");
        dto.setPhone("9876543210"); dto.setStatus("Active");
        return dto;
    }

    private FarmerProfile buildFarmerEntity(User user, String nationalId) {
        return FarmerProfile.builder()
                .user(user).name("Ravi Kumar")
                .dateOfBirth(java.time.LocalDate.of(1990, 5, 15))
                .gender(FarmerProfile.Gender.Male)
                .nationalIdNumber(nationalId)
                .village("Keelapavoor").district("Tirunelveli")
                .state("Tamil Nadu").phone("9876543210")
                .status(FarmerProfile.FarmerStatus.Active).build();
    }

    private LandHoldingRequestDTO buildLandHoldingDTO(Long farmerId, String surveyNumber) {
        LandHoldingRequestDTO dto = new LandHoldingRequestDTO();
        dto.setFarmerId(farmerId); dto.setSurveyNumber(surveyNumber);
        dto.setAreaAcres(new BigDecimal("2.5000")); dto.setSoilType("Loam");
        dto.setIrrigationSource("Borewell"); dto.setOwnershipType("Owned");
        dto.setStatus("Active");
        return dto;
    }

    private LandHolding buildHoldingEntity(FarmerProfile farmer, String surveyNumber) {
        return LandHolding.builder()
                .farmer(farmer).surveyNumber(surveyNumber)
                .areaAcres(new BigDecimal("2.5000"))
                .soilType(LandHolding.SoilType.Loam)
                .irrigationSource(LandHolding.IrrigationSource.Borewell)
                .ownershipType(LandHolding.OwnershipType.Owned)
                .status(LandHolding.LandStatus.Active).build();
    }
}
