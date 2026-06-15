package com.cts.agrilink.farmerLandRegistration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "land_holding",
        indexes = {
                @Index(name = "idxLhFarmerId", columnList = "farmerId"),
                @Index(name = "idxLhStatus",   columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long holdingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farmerId", nullable = false,
            foreignKey = @ForeignKey(name = "fkLhFarmer"))
    private FarmerProfile farmer;

    @Column(nullable = false, length = 50)
    private String surveyNumber;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal areaAcres;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SoilType soilType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IrrigationSource irrigationSource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OwnershipType ownershipType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LandStatus status = LandStatus.Active;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum SoilType {
        Clay, Sandy, Loam, Black
    }

    public enum IrrigationSource {
        Rain, Canal, Borewell, None
    }

    public enum OwnershipType {
        Owned, Leased, SharedCropping
    }

    public enum LandStatus {
        Active, Disputed
    }
}