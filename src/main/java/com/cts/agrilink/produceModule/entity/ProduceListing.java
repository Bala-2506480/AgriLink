package com.cts.agrilink.produceModule.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "produce_listing")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProduceListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ListingID")
    private Long listingId;

    @Column(name = "FarmerID")
    private Long farmerId;

    @Column(name = "CropID")
    private Long cropId;

    @Column(name = "HarvestDate")
    private LocalDate harvestDate;

    @Column(name = "QuantityKg")
    private BigDecimal quantityKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "QualityGrade")
    private QualityGrade qualityGrade;

    @Column(name = "AskingPricePerKg")
    private BigDecimal askingPricePerKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    @Builder.Default
    private ListingStatus status = ListingStatus.Available;

    public enum QualityGrade { A, B, C }
    public enum ListingStatus { Available, PartiallyBooked, Sold, Withdrawn }
}
