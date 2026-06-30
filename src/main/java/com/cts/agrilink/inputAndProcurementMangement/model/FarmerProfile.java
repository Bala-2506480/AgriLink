package com.cts.agrilink.inputAndProcurementMangement.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "farmer_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "farmerId")
    private Long farmerId;

    @Column(name = "userId")
    private Long userId;

    @Column(name = "name")
    private String name;

    @Column(name = "dateOfBirth")
    private LocalDate dateOfBirth;

    @Column(name = "gender")
    private String gender;

    @Column(name = "nationalIdNumber")
    private String nationalIdNumber;

    @Column(name = "village")
    private String village;

    @Column(name = "district")
    private String district;

    @Column(name = "state")
    private String state;

    @Column(name = "phone")
    private String phone;

    @Column(name = "bankAccountNumber")
    private String bankAccountNumber;

    @Column(name = "status")
    private String status;   // Active / Inactive / Verified
}
