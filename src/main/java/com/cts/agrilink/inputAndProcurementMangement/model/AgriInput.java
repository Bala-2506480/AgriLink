package com.cts.agrilink.inputAndProcurementMangement.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "catalog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgriInput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inputId" )
    private Long inputId;

    @Column(name = "name" )
    private String name;

    @Column(name = "category" )
    private String category;

    @Column(name = "unit" )// Fertiliser / Seed / Pesticide / Equipment
    private String unit;

    @Column(name = "pricePerUnit" )
    private Double pricePerUnit;

    @Column(name = "subsidisedPrice" )
    private Double subsidisedPrice;

    @Column(name = "availableStock" )
    private Integer availableStock;

    @Column(name = "status" )
    private String status;     // Available / OutOfStock
}