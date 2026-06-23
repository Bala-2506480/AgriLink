package com.cts.agrilink.inputAndProcurementMangement.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InputRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "requestId")
    private Long requestId;

    @Column(name = "farmerId")
    private Long farmerId;

    @Column(name = "inputId")
    private Long inputId;

    @Column(name = "quantityRequested")
    private Integer quantityRequested;

    @Column(name = "requestDate")
    private LocalDate requestDate;

    @Column(name = "assignedCentreId")
    private Long assignedCentreId;

    @Column(name = "actualPrice")
    private Double actualPrice;

    @Column(name = "status")
    private String status;   // Requested / Approved / Dispatched / Delivered / Cancelled

    @Column(name = "approvedBy")
    private String approvedBy;

    @Column(name = "dispatchedBy")
    private String dispatchedBy;

    @Column(name = "dispatchDate")
    private LocalDate dispatchDate;

    @Column(name = "deliveredDate")
    private LocalDate deliveredDate;

    @Column(name = "receivedBy")
    private String receivedBy;

    @Column(name = "cancellationReason")
    private String cancellationReason;
}
