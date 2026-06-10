package com.cts.agrilink.inputAndProcurementMangement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveRequestDTO {

    @NotNull(message = "Assigned centre ID is required")
    private Long assignedCentreId;

    @NotNull(message = "Actual price is required")
    private Double actualPrice;

    @NotBlank(message = "Approved by is required")
    private String approvedBy;
}
