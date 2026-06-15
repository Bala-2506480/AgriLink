package com.cts.agrilink.farmerLandRegistration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "farmer_profile",
        indexes = {
                @Index(name = "idxFpUserId", columnList = "userId"),
                @Index(name = "idxFpStatus", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long farmerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", nullable = false,
            foreignKey = @ForeignKey(name = "fkFpUser"))
    private User user;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false, unique = true, length = 50)
    private String nationalIdNumber;

    @Column(nullable = false, length = 100)
    private String village;

    @Column(nullable = false, length = 100)
    private String district;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(length = 30)
    private String bankAccountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FarmerStatus status = FarmerStatus.Active;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum Gender {
        Male, Female, Other
    }

    public enum FarmerStatus {
        Active, Inactive, Verified
    }
}