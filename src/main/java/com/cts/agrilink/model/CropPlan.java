package com.cts.agrilink.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "crop_plan")
public class CropPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "planId")
    private Integer planId;

    @Column(name = "farmerId", nullable = false)
    private Long farmerId;

    @Column(name = "holdingId", nullable = false)
    private Long holdingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cropId", nullable = false)
    private CropCatalog cropCatalog;

    @Enumerated(EnumType.STRING)
    @Column(name = "planSeason", nullable = false)
    private PlanSeason planSeason;

    @Column(name = "planYear", nullable = false)
    private Integer planYear;

    @Column(name = "sowingDate", nullable = false)
    private LocalDate sowingDate;

    @Column(name = "expectedHarvestDate", nullable = false)
    private LocalDate expectedHarvestDate;

    @Column(name = "areaPlanted", nullable = false, precision = 8, scale = 2)
    private BigDecimal areaPlanted;

    @Enumerated(EnumType.STRING)
    @Column(name = "planStatus", nullable = false)
    private PlanStatus planStatus = PlanStatus.Pl;

    @Column(name = "approvedBy")
    private Integer approvedBy;

    @Column(name = "approvedAt")
    private LocalDateTime approvedAt;

    @OneToMany(mappedBy = "cropPlan", fetch = FetchType.LAZY)
    private List<GrowthObservation> growthObservations;

    public CropPlan() {}

    public Integer getPlanId() { return planId; }
    public void setPlanId(Integer planId) { this.planId = planId; }

    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }

    public Long getHoldingId() { return holdingId; }
    public void setHoldingId(Long holdingId) { this.holdingId = holdingId; }

    public CropCatalog getCropCatalog() { return cropCatalog; }
    public void setCropCatalog(CropCatalog cropCatalog) { this.cropCatalog = cropCatalog; }

    public PlanSeason getPlanSeason() { return planSeason; }
    public void setPlanSeason(PlanSeason planSeason) { this.planSeason = planSeason; }

    public Integer getPlanYear() { return planYear; }
    public void setPlanYear(Integer planYear) { this.planYear = planYear; }

    public LocalDate getSowingDate() { return sowingDate; }
    public void setSowingDate(LocalDate sowingDate) { this.sowingDate = sowingDate; }

    public LocalDate getExpectedHarvestDate() { return expectedHarvestDate; }
    public void setExpectedHarvestDate(LocalDate expectedHarvestDate) { this.expectedHarvestDate = expectedHarvestDate; }

    public BigDecimal getAreaPlanted() { return areaPlanted; }
    public void setAreaPlanted(BigDecimal areaPlanted) { this.areaPlanted = areaPlanted; }

    public PlanStatus getPlanStatus() { return planStatus; }
    public void setPlanStatus(PlanStatus planStatus) { this.planStatus = planStatus; }

    public Integer getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Integer approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public List<GrowthObservation> getGrowthObservations() { return growthObservations; }
    public void setGrowthObservations(List<GrowthObservation> growthObservations) { this.growthObservations = growthObservations; }

    public enum PlanSeason {
        Kharif, Rabi, Zaid, Perennial
    }

    public enum PlanStatus {
        Pl, So, Gr, Ha, Fa, Ca;

        public boolean canTransitionTo(PlanStatus next) {
            return switch (this) {
                case Pl -> next == So || next == Ca;
                case So -> next == Gr || next == Fa;
                case Gr -> next == Ha || next == Fa;
                default -> false;
            };
        }
    }
}
