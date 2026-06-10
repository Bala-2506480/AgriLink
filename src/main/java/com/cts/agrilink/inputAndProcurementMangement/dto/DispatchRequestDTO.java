package com.cts.agrilink.inputAndProcurementMangement.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispatchRequestDTO {

    @NotBlank(message = "Dispatched by is required")
    private String dispatchedBy;

    @NotNull(message = "Dispatch date is required")
    private LocalDate dispatchDate;
}
