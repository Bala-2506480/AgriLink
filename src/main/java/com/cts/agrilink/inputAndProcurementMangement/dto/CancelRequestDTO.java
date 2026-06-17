package com.cts.agrilink.inputAndProcurementMangement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelRequestDTO {

    @NotBlank(message = "Reason is required")
    private String reason;
}
