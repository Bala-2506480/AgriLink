package com.cts.agrilink.farmerLandRegistration.dto;

import com.cts.agrilink.farmerLandRegistration.model.LandHolding;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateLandHoldingRequestDto {

    @DecimalMin(value = "0.0001", message = "areaAcres must be positive")
    private BigDecimal areaAcres;

    private LandHolding.SoilType soilType;

    private LandHolding.IrrigationSource irrigationSource;

    private LandHolding.OwnershipType ownershipType;

    private LandHolding.Status status;
}
