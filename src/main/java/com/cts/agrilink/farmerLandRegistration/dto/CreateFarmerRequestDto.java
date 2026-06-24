package com.cts.agrilink.farmerLandRegistration.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFarmerRequestDto {

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "gender is required")
    private FarmerProfile.Gender gender;

    @NotNull(message = "dateOfBirth is required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "phone is required")
    private String phone;

    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "nationalIdNumber is required")
    private String nationalIdNumber;

    @NotBlank(message = "state is required")
    private String state;

    @NotBlank(message = "district is required")
    private String district;

    @NotBlank(message = "village is required")
    private String village;

    private String bankAccountNumber;
}