package com.cts.agrilink.dto;

import com.cts.agrilink.model.CropCatalog;
import com.cts.agrilink.model.CropPlan;
import com.cts.agrilink.model.GrowthObservation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AgrilinkDto {

    // ── CROP CATALOG ──────────────────────────────────────────

    public static class CropCatalogCreateRequest {
        @NotBlank(message = "cropName is required")
        private String cropName;
        @NotNull(message = "cropCategory is required")
        private CropCatalog.CropCategory cropCategory;
        @NotNull(message = "cropSeason is required")
        private CropCatalog.CropSeason cropSeason;
        @NotNull(message = "typicalDurationDays is required")
        @Min(value = 1)
        private Integer typicalDurationDays;
        @NotNull(message = "expectedYieldPerAcre is required")
        @DecimalMin(value = "0.01")
        private BigDecimal expectedYieldPerAcre;
        private CropCatalog.CatalogStatus catalogStatus = CropCatalog.CatalogStatus.Ac;

        public String getCropName() { return cropName; }
        public void setCropName(String v) { this.cropName = v; }
        public CropCatalog.CropCategory getCropCategory() { return cropCategory; }
        public void setCropCategory(CropCatalog.CropCategory v) { this.cropCategory = v; }
        public CropCatalog.CropSeason getCropSeason() { return cropSeason; }
        public void setCropSeason(CropCatalog.CropSeason v) { this.cropSeason = v; }
        public Integer getTypicalDurationDays() { return typicalDurationDays; }
        public void setTypicalDurationDays(Integer v) { this.typicalDurationDays = v; }
        public BigDecimal getExpectedYieldPerAcre() { return expectedYieldPerAcre; }
        public void setExpectedYieldPerAcre(BigDecimal v) { this.expectedYieldPerAcre = v; }
        public CropCatalog.CatalogStatus getCatalogStatus() { return catalogStatus; }
        public void setCatalogStatus(CropCatalog.CatalogStatus v) { this.catalogStatus = v; }
    }

    public static class CropCatalogUpdateRequest {
        private Integer typicalDurationDays;
        private BigDecimal expectedYieldPerAcre;
        private CropCatalog.CatalogStatus catalogStatus;

        public Integer getTypicalDurationDays() { return typicalDurationDays; }
        public void setTypicalDurationDays(Integer v) { this.typicalDurationDays = v; }
        public BigDecimal getExpectedYieldPerAcre() { return expectedYieldPerAcre; }
        public void setExpectedYieldPerAcre(BigDecimal v) { this.expectedYieldPerAcre = v; }
        public CropCatalog.CatalogStatus getCatalogStatus() { return catalogStatus; }
        public void setCatalogStatus(CropCatalog.CatalogStatus v) { this.catalogStatus = v; }
    }

    public static class CropCatalogResponse {
        private Integer cropId;
        private String cropName;
        private CropCatalog.CropCategory cropCategory;
        private CropCatalog.CropSeason cropSeason;
        private Integer typicalDurationDays;
        private BigDecimal expectedYieldPerAcre;
        private CropCatalog.CatalogStatus catalogStatus;

        public CropCatalogResponse() {}
        public CropCatalogResponse(Integer cropId, String cropName,
                CropCatalog.CropCategory cropCategory, CropCatalog.CropSeason cropSeason,
                Integer typicalDurationDays, BigDecimal expectedYieldPerAcre,
                CropCatalog.CatalogStatus catalogStatus) {
            this.cropId = cropId; this.cropName = cropName;
            this.cropCategory = cropCategory; this.cropSeason = cropSeason;
            this.typicalDurationDays = typicalDurationDays;
            this.expectedYieldPerAcre = expectedYieldPerAcre;
            this.catalogStatus = catalogStatus;
        }

        public static CropCatalogResponse from(CropCatalog c) {
            return new CropCatalogResponse(c.getCropId(), c.getCropName(),
                    c.getCropCategory(), c.getCropSeason(),
                    c.getTypicalDurationDays(), c.getExpectedYieldPerAcre(),
                    c.getCatalogStatus());
        }

        public Integer getCropId() { return cropId; }
        public String getCropName() { return cropName; }
        public CropCatalog.CropCategory getCropCategory() { return cropCategory; }
        public CropCatalog.CropSeason getCropSeason() { return cropSeason; }
        public Integer getTypicalDurationDays() { return typicalDurationDays; }
        public BigDecimal getExpectedYieldPerAcre() { return expectedYieldPerAcre; }
        public CropCatalog.CatalogStatus getCatalogStatus() { return catalogStatus; }
    }

    // ── CROP PLAN ─────────────────────────────────────────────

    public static class CropPlanCreateRequest {
        @NotNull(message = "farmerId is required")
        private Long farmerId;
        @NotNull(message = "holdingId is required")
        private Long holdingId;
        @NotNull(message = "cropId is required")
        private Integer cropId;
        @NotNull(message = "planSeason is required")
        private CropPlan.PlanSeason planSeason;
        @NotNull(message = "planYear is required")
        private Integer planYear;
        @NotNull(message = "sowingDate is required")
        private LocalDate sowingDate;
        @NotNull(message = "expectedHarvestDate is required")
        private LocalDate expectedHarvestDate;
        @NotNull(message = "areaPlanted is required")
        @DecimalMin(value = "0.01")
        private BigDecimal areaPlanted;
        private CropPlan.PlanStatus planStatus = CropPlan.PlanStatus.Pl;

        public Long getFarmerId() { return farmerId; }
        public void setFarmerId(Long v) { this.farmerId = v; }
        public Long getHoldingId() { return holdingId; }
        public void setHoldingId(Long v) { this.holdingId = v; }
        public Integer getCropId() { return cropId; }
        public void setCropId(Integer v) { this.cropId = v; }
        public CropPlan.PlanSeason getPlanSeason() { return planSeason; }
        public void setPlanSeason(CropPlan.PlanSeason v) { this.planSeason = v; }
        public Integer getPlanYear() { return planYear; }
        public void setPlanYear(Integer v) { this.planYear = v; }
        public LocalDate getSowingDate() { return sowingDate; }
        public void setSowingDate(LocalDate v) { this.sowingDate = v; }
        public LocalDate getExpectedHarvestDate() { return expectedHarvestDate; }
        public void setExpectedHarvestDate(LocalDate v) { this.expectedHarvestDate = v; }
        public BigDecimal getAreaPlanted() { return areaPlanted; }
        public void setAreaPlanted(BigDecimal v) { this.areaPlanted = v; }
        public CropPlan.PlanStatus getPlanStatus() { return planStatus; }
        public void setPlanStatus(CropPlan.PlanStatus v) { this.planStatus = v; }
    }

    public static class CropPlanUpdateRequest {
        private LocalDate sowingDate;
        private LocalDate expectedHarvestDate;
        private BigDecimal areaPlanted;

        public LocalDate getSowingDate() { return sowingDate; }
        public void setSowingDate(LocalDate v) { this.sowingDate = v; }
        public LocalDate getExpectedHarvestDate() { return expectedHarvestDate; }
        public void setExpectedHarvestDate(LocalDate v) { this.expectedHarvestDate = v; }
        public BigDecimal getAreaPlanted() { return areaPlanted; }
        public void setAreaPlanted(BigDecimal v) { this.areaPlanted = v; }
    }

    public static class CropPlanStatusRequest {
        @NotNull(message = "planStatus is required")
        private CropPlan.PlanStatus planStatus;

        public CropPlan.PlanStatus getPlanStatus() { return planStatus; }
        public void setPlanStatus(CropPlan.PlanStatus v) { this.planStatus = v; }
    }

    public static class CropPlanApproveRequest {
        @NotNull(message = "approvedBy is required")
        private Integer approvedBy;

        public Integer getApprovedBy() { return approvedBy; }
        public void setApprovedBy(Integer v) { this.approvedBy = v; }
    }

    public static class CropPlanResponse {
        private Integer planId;
        private Long farmerId;
        private Long holdingId;
        private Integer cropId;
        private String cropName;
        private CropPlan.PlanSeason planSeason;
        private Integer planYear;
        private LocalDate sowingDate;
        private LocalDate expectedHarvestDate;
        private BigDecimal areaPlanted;
        private CropPlan.PlanStatus planStatus;
        private Integer approvedBy;
        private LocalDateTime approvedAt;

        public CropPlanResponse() {}

        public static CropPlanResponse from(CropPlan p) {
            CropPlanResponse r = new CropPlanResponse();
            r.planId = p.getPlanId();
            r.farmerId = p.getFarmerId();
            r.holdingId = p.getHoldingId();
            r.cropId = p.getCropCatalog() != null ? p.getCropCatalog().getCropId() : null;
            r.cropName = p.getCropCatalog() != null ? p.getCropCatalog().getCropName() : null;
            r.planSeason = p.getPlanSeason();
            r.planYear = p.getPlanYear();
            r.sowingDate = p.getSowingDate();
            r.expectedHarvestDate = p.getExpectedHarvestDate();
            r.areaPlanted = p.getAreaPlanted();
            r.planStatus = p.getPlanStatus();
            r.approvedBy = p.getApprovedBy();
            r.approvedAt = p.getApprovedAt();
            return r;
        }

        public Integer getPlanId() { return planId; }
        public Long getFarmerId() { return farmerId; }
        public Long getHoldingId() { return holdingId; }
        public Integer getCropId() { return cropId; }
        public String getCropName() { return cropName; }
        public CropPlan.PlanSeason getPlanSeason() { return planSeason; }
        public Integer getPlanYear() { return planYear; }
        public LocalDate getSowingDate() { return sowingDate; }
        public LocalDate getExpectedHarvestDate() { return expectedHarvestDate; }
        public BigDecimal getAreaPlanted() { return areaPlanted; }
        public CropPlan.PlanStatus getPlanStatus() { return planStatus; }
        public Integer getApprovedBy() { return approvedBy; }
        public LocalDateTime getApprovedAt() { return approvedAt; }
    }

    // ── GROWTH OBSERVATION ────────────────────────────────────

    public static class ObservationCreateRequest {
        @NotNull(message = "planId is required")
        private Integer planId;
        @NotNull(message = "officerId is required")
        private Integer officerId;
        @NotNull(message = "observationDate is required")
        private LocalDate observationDate;
        @NotNull(message = "growthStage is required")
        private GrowthObservation.GrowthStage growthStage;
        private Boolean pestOrDiseaseFlag = false;
        private String fieldRemarks;

        public Integer getPlanId() { return planId; }
        public void setPlanId(Integer v) { this.planId = v; }
        public Integer getOfficerId() { return officerId; }
        public void setOfficerId(Integer v) { this.officerId = v; }
        public LocalDate getObservationDate() { return observationDate; }
        public void setObservationDate(LocalDate v) { this.observationDate = v; }
        public GrowthObservation.GrowthStage getGrowthStage() { return growthStage; }
        public void setGrowthStage(GrowthObservation.GrowthStage v) { this.growthStage = v; }
        public Boolean getPestOrDiseaseFlag() { return pestOrDiseaseFlag; }
        public void setPestOrDiseaseFlag(Boolean v) { this.pestOrDiseaseFlag = v; }
        public String getFieldRemarks() { return fieldRemarks; }
        public void setFieldRemarks(String v) { this.fieldRemarks = v; }
    }

    public static class ObservationUpdateRequest {
        private GrowthObservation.GrowthStage growthStage;
        private Boolean pestOrDiseaseFlag;
        private String fieldRemarks;

        public GrowthObservation.GrowthStage getGrowthStage() { return growthStage; }
        public void setGrowthStage(GrowthObservation.GrowthStage v) { this.growthStage = v; }
        public Boolean getPestOrDiseaseFlag() { return pestOrDiseaseFlag; }
        public void setPestOrDiseaseFlag(Boolean v) { this.pestOrDiseaseFlag = v; }
        public String getFieldRemarks() { return fieldRemarks; }
        public void setFieldRemarks(String v) { this.fieldRemarks = v; }
    }

    public static class ObservationPestFlagRequest {
        @NotNull(message = "pestOrDiseaseFlag is required")
        private Boolean pestOrDiseaseFlag;

        public Boolean getPestOrDiseaseFlag() { return pestOrDiseaseFlag; }
        public void setPestOrDiseaseFlag(Boolean v) { this.pestOrDiseaseFlag = v; }
    }

    public static class ObservationResponse {
        private Integer observationId;
        private Integer planId;
        private Integer officerId;
        private LocalDate observationDate;
        private GrowthObservation.GrowthStage growthStage;
        private Boolean pestOrDiseaseFlag;
        private String fieldRemarks;
        private LocalDateTime createdAt;

        public ObservationResponse() {}

        public static ObservationResponse from(GrowthObservation g) {
            ObservationResponse r = new ObservationResponse();
            r.observationId = g.getObservationId();
            r.planId = g.getCropPlan() != null ? g.getCropPlan().getPlanId() : null;
            r.officerId = g.getOfficerId();
            r.observationDate = g.getObservationDate();
            r.growthStage = g.getGrowthStage();
            r.pestOrDiseaseFlag = g.getPestOrDiseaseFlag();
            r.fieldRemarks = g.getFieldRemarks();
            r.createdAt = g.getCreatedAt();
            return r;
        }

        public Integer getObservationId() { return observationId; }
        public Integer getPlanId() { return planId; }
        public Integer getOfficerId() { return officerId; }
        public LocalDate getObservationDate() { return observationDate; }
        public GrowthObservation.GrowthStage getGrowthStage() { return growthStage; }
        public Boolean getPestOrDiseaseFlag() { return pestOrDiseaseFlag; }
        public String getFieldRemarks() { return fieldRemarks; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
}