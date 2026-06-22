package com.cts.agrilink.farmerLandRegistration.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "land_holding")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holdingId")
    private Long holdingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmerId", nullable = false)
    private FarmerProfile farmer;

    @Column(name = "surveyNumber", nullable = false, length = 50)
    private String surveyNumber;

    @Column(name = "areaAcres", nullable = false, precision = 10, scale = 4)
    private BigDecimal areaAcres;

    @Enumerated(EnumType.STRING)
    @Column(name = "soilType", nullable = false)
    private SoilType soilType;

    @Enumerated(EnumType.STRING)
    @Column(name = "irrigationSource", nullable = false)
    private IrrigationSource irrigationSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "ownershipType", nullable = false)
    private OwnershipType ownershipType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Status status = Status.Active;

    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum SoilType       { Clay, Sandy, Loam, Black }
    public enum IrrigationSource { Rain, Canal, Borewell, None }
    public enum OwnershipType  { Owned, Leased, SharedCropping }
    public enum Status         { Active, Disputed }
}
