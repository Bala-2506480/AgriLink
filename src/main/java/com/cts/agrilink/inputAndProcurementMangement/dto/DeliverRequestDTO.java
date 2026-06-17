package com.cts.agrilink.inputAndProcurementMangement.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliverRequestDTO {

    @NotNull(message = "Delivered date is required")
    private LocalDate deliveredDate;

    @NotBlank(message = "Received by is required")
    private String receivedBy;
}
