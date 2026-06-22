package com.cts.agrilink.farmerLandRegistration.dto;

import com.cts.agrilink.farmerLandRegistration.model.LandHolding;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateLandHoldingRequestDto {

    @NotNull(message = "farmerId is required")
    private Long farmerId;

    @NotBlank(message = "surveyNumber is required")
    @Size(max = 50)
    private String surveyNumber;

    @NotNull(message = "areaAcres is required")
    @DecimalMin(value = "0.0001", message = "areaAcres must be positive")
    private BigDecimal areaAcres;

    @NotNull(message = "soilType is required")
    private LandHolding.SoilType soilType;

    @NotNull(message = "irrigationSource is required")
    private LandHolding.IrrigationSource irrigationSource;

    @NotNull(message = "ownershipType is required")
    private LandHolding.OwnershipType ownershipType;
}
