package com.cts.agrilink.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "growth_observation")
public class GrowthObservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "observationId")
    private Integer observationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planId", nullable = false)
    private CropPlan cropPlan;

    @Column(name = "officerId", nullable = false)
    private Integer officerId;

    @Column(name = "observationDate", nullable = false)
    private LocalDate observationDate;

    @Convert(converter = GrowthStageConverter.class)
    @Column(name = "growthStage", nullable = false)
    private GrowthStage growthStage;

    @Column(name = "pestOrDiseaseFlag", nullable = false)
    private Boolean pestOrDiseaseFlag = false;

    @Column(name = "fieldRemarks", columnDefinition = "TEXT")
    private String fieldRemarks;

    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public GrowthObservation() {}

    public Integer getObservationId() { return observationId; }
    public void setObservationId(Integer observationId) { this.observationId = observationId; }

    public CropPlan getCropPlan() { return cropPlan; }
    public void setCropPlan(CropPlan cropPlan) { this.cropPlan = cropPlan; }

    public Integer getOfficerId() { return officerId; }
    public void setOfficerId(Integer officerId) { this.officerId = officerId; }

    public LocalDate getObservationDate() { return observationDate; }
    public void setObservationDate(LocalDate observationDate) { this.observationDate = observationDate; }

    public GrowthStage getGrowthStage() { return growthStage; }
    public void setGrowthStage(GrowthStage growthStage) { this.growthStage = growthStage; }

    public Boolean getPestOrDiseaseFlag() { return pestOrDiseaseFlag; }
    public void setPestOrDiseaseFlag(Boolean pestOrDiseaseFlag) { this.pestOrDiseaseFlag = pestOrDiseaseFlag; }

    public String getFieldRemarks() { return fieldRemarks; }
    public void setFieldRemarks(String fieldRemarks) { this.fieldRemarks = fieldRemarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public enum GrowthStage {
        Germination, Vegetative, Tillering, Flowering,
        Grain_filling, Maturity, Harvest_ready
    }
}
