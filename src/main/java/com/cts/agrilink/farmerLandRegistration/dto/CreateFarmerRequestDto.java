package com.cts.agrilink.farmerLandRegistration.dto;

import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

// ── Create ────────────────────────────────────────────────────────────────────

@Data
public class CreateFarmerRequestDto {

    @NotNull(message = "userId is required")
    private Integer userId;

    @NotBlank(message = "name is required")
    @Size(max = 150)
    private String name;

    @NotNull(message = "dateOfBirth is required")
    private LocalDate dateOfBirth;

    @NotNull(message = "gender is required")
    private FarmerProfile.Gender gender;

    @NotBlank(message = "nationalIdNumber is required")
    @Size(max = 50)
    private String nationalIdNumber;

    @NotBlank(message = "village is required")
    @Size(max = 100)
    private String village;

    @NotBlank(message = "district is required")
    @Size(max = 100)
    private String district;

    @NotBlank(message = "state is required")
    @Size(max = 100)
    private String state;

    @NotBlank(message = "phone is required")
    @Size(max = 15)
    private String phone;

    @Size(max = 30)
    private String bankAccountNumber;
}
