package com.cts.agrilink.inputAndProcurementMangement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockDTO {

    @NotNull(message = "Available stock is required")
    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer availableStock;
}
