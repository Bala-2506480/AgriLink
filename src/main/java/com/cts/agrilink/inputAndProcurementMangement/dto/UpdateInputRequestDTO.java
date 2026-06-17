package com.cts.agrilink.inputAndProcurementMangement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInputRequestDTO {

    @Positive(message = "Quantity must be positive")
    private Integer quantityRequested;

    private Long assignedCentreId;

    private Double actualPrice;
}
