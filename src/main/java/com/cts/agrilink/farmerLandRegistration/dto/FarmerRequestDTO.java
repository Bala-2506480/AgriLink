package com.cts.agrilink.farmerLandRegistration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FarmerRequestDTO {

    @NotNull(message = "userId is mandatory")
    private Long userId;

    @NotBlank(message = "Name is mandatory")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Date of birth is mandatory")
    private String dateOfBirth;

    @NotBlank(message = "Gender is mandatory")
    private String gender;

    @NotBlank(message = "National ID Number is mandatory")
    @Size(max = 50)
    private String nationalIdNumber;

    @NotBlank(message = "Village is mandatory")
    @Size(max = 100)
    private String village;

    @NotBlank(message = "District is mandatory")
    @Size(max = 100)
    private String district;

    @NotBlank(message = "State is mandatory")
    @Size(max = 100)
    private String state;

    @NotBlank(message = "Phone is mandatory")
    @Size(max = 15)
    private String phone;

    @Size(max = 30)
    private String bankAccountNumber;

    private String status;
}
