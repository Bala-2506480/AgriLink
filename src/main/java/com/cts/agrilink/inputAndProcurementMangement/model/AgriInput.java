package com.cts.agrilink.inputAndProcurementMangement.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "input_catalog")
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

    @Column(name = "unit" ) // Fertiliser / Seed / Pesticide / Equipment
    private String unit;

    @Column(name = "pricePerUnit" )
    private Double pricePerUnit;

    @Column(name = "subsidisedPrice" )
    private Double subsidisedPrice;

    @Column(name = "availableStock" )
    private Integer availableStock;

    @Column(name = "status" )
    private String status;     // Available / OutOfStock

    // One-to-Many Relationship to InputRequest
    @OneToMany(mappedBy = "agriInput", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Prevents infinite recursion during JSON serialization
    @ToString.Exclude     // Prevents infinite loop in Lombok's toString()
    @EqualsAndHashCode.Exclude
    private List<InputRequest> inputRequests;
}