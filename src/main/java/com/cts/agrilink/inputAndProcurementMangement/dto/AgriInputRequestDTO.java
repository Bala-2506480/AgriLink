package com.cts.agrilink.inputAndProcurementMangement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgriInputRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "Price per unit is required")
    @Positive(message = "Price must be positive")
    private Double pricePerUnit;

    private Double subsidisedPrice;

    @NotNull(message = "Available stock is required")
    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer availableStock;

    @NotBlank(message = "Status is required")
    private String status;
}