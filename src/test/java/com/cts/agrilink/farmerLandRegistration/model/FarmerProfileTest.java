package com.cts.agrilink.farmerLandRegistration.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class FarmerProfileTest {

    @Test
    void builder_SetsAllFields() {
        User user = User.builder().userId(1L).name("Ravi").email("r@e.com")
                .status(User.UserStatus.Active).build();

        FarmerProfile farmer = FarmerProfile.builder()
                .farmerId(1L).user(user).name("Ravi Kumar")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(FarmerProfile.Gender.Male)
                .nationalIdNumber("NID001").village("Keelapavoor")
                .district("Tirunelveli").state("Tamil Nadu")
                .phone("9876543210").bankAccountNumber("ACC001")
                .status(FarmerProfile.FarmerStatus.Active).build();

        assertEquals(1L, farmer.getFarmerId());
        assertEquals("NID001", farmer.getNationalIdNumber());
        assertEquals(FarmerProfile.Gender.Male, farmer.getGender());
        assertEquals(FarmerProfile.FarmerStatus.Active, farmer.getStatus());
    }

    @Test
    void setter_UpdatesStatusAndVillage() {
        FarmerProfile farmer = new FarmerProfile();
        farmer.setStatus(FarmerProfile.FarmerStatus.Verified);
        farmer.setVillage("Palayamkottai");
        assertEquals(FarmerProfile.FarmerStatus.Verified, farmer.getStatus());
        assertEquals("Palayamkottai", farmer.getVillage());
    }

    @Test
    void genderEnum_AllValues() {
        assertEquals(3, FarmerProfile.Gender.values().length);
        assertEquals(FarmerProfile.Gender.Female, FarmerProfile.Gender.valueOf("Female"));
        assertThrows(IllegalArgumentException.class, () -> FarmerProfile.Gender.valueOf("INVALID"));
    }

    @Test
    void statusEnum_AllValues() {
        assertEquals(3, FarmerProfile.FarmerStatus.values().length);
        assertEquals(FarmerProfile.FarmerStatus.Verified, FarmerProfile.FarmerStatus.valueOf("Verified"));
    }
}
