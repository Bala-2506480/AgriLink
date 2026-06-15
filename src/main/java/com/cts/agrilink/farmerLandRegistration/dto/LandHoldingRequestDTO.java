package com.cts.agrilink.farmerLandRegistration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LandHoldingRequestDTO {

    @NotNull(message = "farmerId is mandatory")
    private Long farmerId;

    @NotBlank(message = "Survey number is mandatory")
    @Size(max = 50)
    private String surveyNumber;

    @NotNull(message = "Area in acres is mandatory")
    @Positive(message = "Area must be positive")
    private BigDecimal areaAcres;

    @NotBlank(message = "Soil type is mandatory")
    private String soilType;

    @NotBlank(message = "Irrigation source is mandatory")
    private String irrigationSource;

    @NotBlank(message = "Ownership type is mandatory")
    private String ownershipType;

    private String status;
}
