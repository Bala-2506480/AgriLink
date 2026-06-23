package com.cts.agrilink.produceModule.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "produce_sale")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProduceSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SaleID")
    private Long saleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ListingID")
    private ProduceListing listing;

    @Column(name = "BuyerID")
    private Long buyerId;

    @Column(name = "QuantitySoldKg")
    private BigDecimal quantitySoldKg;

    @Column(name = "AgreedPricePerKg")
    private BigDecimal agreedPricePerKg;

    @Column(name = "TotalAmount")
    private BigDecimal totalAmount;

    @Column(name = "SaleDate")
    private LocalDate saleDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "PaymentStatus")
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.Pending;

    public enum PaymentStatus { Pending, Paid, Overdue }
}
