package com.cts.agrilink.farmerLandRegistration.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class LandHoldingTest {

    @Test
    void builder_SetsAllFields() {
        User user = User.builder().userId(1L).name("Ravi").email("r@e.com")
                .status(User.UserStatus.Active).build();
        FarmerProfile farmer = FarmerProfile.builder()
                .farmerId(1L).user(user).name("Ravi")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(FarmerProfile.Gender.Male).nationalIdNumber("NID001")
                .village("V").district("D").state("S").phone("9876543210")
                .status(FarmerProfile.FarmerStatus.Active).build();

        LandHolding holding = LandHolding.builder()
                .holdingId(1L).farmer(farmer).surveyNumber("SRV-001")
                .areaAcres(new BigDecimal("2.5000"))
                .soilType(LandHolding.SoilType.Loam)
                .irrigationSource(LandHolding.IrrigationSource.Borewell)
                .ownershipType(LandHolding.OwnershipType.Owned)
                .status(LandHolding.LandStatus.Active).build();

        assertEquals("SRV-001", holding.getSurveyNumber());
        assertEquals(LandHolding.SoilType.Loam, holding.getSoilType());
        assertEquals(LandHolding.LandStatus.Active, holding.getStatus());
    }

    @Test
    void setter_UpdatesFields() {
        LandHolding holding = new LandHolding();
        holding.setAreaAcres(new BigDecimal("4.0000"));
        holding.setSoilType(LandHolding.SoilType.Clay);
        holding.setStatus(LandHolding.LandStatus.Disputed);
        assertEquals(new BigDecimal("4.0000"), holding.getAreaAcres());
        assertEquals(LandHolding.SoilType.Clay, holding.getSoilType());
        assertEquals(LandHolding.LandStatus.Disputed, holding.getStatus());
    }

    @Test
    void allEnums_ValueOfAndCount() {
        assertEquals(4, LandHolding.SoilType.values().length);
        assertEquals(4, LandHolding.IrrigationSource.values().length);
        assertEquals(3, LandHolding.OwnershipType.values().length);
        assertEquals(2, LandHolding.LandStatus.values().length);
        assertEquals(LandHolding.OwnershipType.SharedCropping, LandHolding.OwnershipType.valueOf("SharedCropping"));
        assertThrows(IllegalArgumentException.class, () -> LandHolding.SoilType.valueOf("INVALID"));
    }
}
