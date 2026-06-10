package com.cts.agrilink.inputAndProcurementMangement.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InputProcurementRequestDTO {

    @NotNull(message = "Farmer ID is required")
    private Long farmerId;

    @NotNull(message = "Input ID is required")
    private Long inputId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantityRequested;

    @NotNull(message = "Request date is required")
    private LocalDate requestDate;

    @NotNull(message = "Assigned centre ID is required")
    private Long assignedCentreId;

    // ADD THIS
    private Double actualPrice;
}