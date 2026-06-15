package com.cts.agrilink.farmerLandRegistration.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ===== ApiResponseDTO =====

    @Test
    void apiResponse_GetSet_Message() {
        ApiResponseDTO dto = new ApiResponseDTO("ok");
        assertEquals("ok", dto.getMessage());
        dto.setMessage("updated");
        assertEquals("updated", dto.getMessage());
    }

    // ===== UserRequestDTO =====

    @Test
    void userDTO_Valid_NoViolations() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Ravi"); dto.setEmail("r@e.com"); dto.setPasswordHash("hash");
        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void userDTO_MissingName_HasViolation() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName(""); dto.setEmail("r@e.com"); dto.setPasswordHash("hash");
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void userDTO_MissingEmail_HasViolation() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Ravi"); dto.setEmail(""); dto.setPasswordHash("hash");
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void userDTO_MissingPassword_HasViolation() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Ravi"); dto.setEmail("r@e.com"); dto.setPasswordHash("");
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void userDTO_PhoneOptional_NoViolation() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Ravi"); dto.setEmail("r@e.com"); dto.setPasswordHash("hash");
        dto.setPhone(null);
        assertTrue(validator.validate(dto).isEmpty());
    }

    // ===== FarmerRequestDTO =====

    @Test
    void farmerDTO_Valid_NoViolations() {
        assertTrue(validator.validate(buildValidFarmerDTO()).isEmpty());
    }

    @Test
    void farmerDTO_NullUserId_HasViolation() {
        FarmerRequestDTO dto = buildValidFarmerDTO();
        dto.setUserId(null);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void farmerDTO_BlankName_HasViolation() {
        FarmerRequestDTO dto = buildValidFarmerDTO();
        dto.setName("");
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void farmerDTO_BankAccountAndStatusOptional_NoViolation() {
        FarmerRequestDTO dto = buildValidFarmerDTO();
        dto.setBankAccountNumber(null);
        dto.setStatus(null);
        assertTrue(validator.validate(dto).isEmpty());
    }

    // ===== LandHoldingRequestDTO =====

    @Test
    void landHoldingDTO_Valid_NoViolations() {
        assertTrue(validator.validate(buildValidLandHoldingDTO()).isEmpty());
    }

    @Test
    void landHoldingDTO_NullFarmerId_HasViolation() {
        LandHoldingRequestDTO dto = buildValidLandHoldingDTO();
        dto.setFarmerId(null);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void landHoldingDTO_NullAreaAcres_HasViolation() {
        LandHoldingRequestDTO dto = buildValidLandHoldingDTO();
        dto.setAreaAcres(null);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void landHoldingDTO_NegativeAreaAcres_HasViolation() {
        LandHoldingRequestDTO dto = buildValidLandHoldingDTO();
        dto.setAreaAcres(new BigDecimal("-1.0"));
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void landHoldingDTO_BlankSoilType_HasViolation() {
        LandHoldingRequestDTO dto = buildValidLandHoldingDTO();
        dto.setSoilType("");
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void landHoldingDTO_StatusOptional_NoViolation() {
        LandHoldingRequestDTO dto = buildValidLandHoldingDTO();
        dto.setStatus(null);
        assertTrue(validator.validate(dto).isEmpty());
    }

    // ===== Helpers =====

    private FarmerRequestDTO buildValidFarmerDTO() {
        FarmerRequestDTO dto = new FarmerRequestDTO();
        dto.setUserId(1L); dto.setName("Ravi"); dto.setDateOfBirth("1990-05-15");
        dto.setGender("Male"); dto.setNationalIdNumber("NID001");
        dto.setVillage("V"); dto.setDistrict("D"); dto.setState("S");
        dto.setPhone("9876543210"); dto.setStatus("Active");
        return dto;
    }

    private LandHoldingRequestDTO buildValidLandHoldingDTO() {
        LandHoldingRequestDTO dto = new LandHoldingRequestDTO();
        dto.setFarmerId(1L); dto.setSurveyNumber("SRV-001");
        dto.setAreaAcres(new BigDecimal("2.5000"));
        dto.setSoilType("Loam"); dto.setIrrigationSource("Borewell");
        dto.setOwnershipType("Owned"); dto.setStatus("Active");
        return dto;
    }
}
