package com.cts.agrilink.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "crop_catalog")
public class CropCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cropId")
    private Integer cropId;

    @Column(name = "cropName", nullable = false, length = 100)
    private String cropName;

    @Enumerated(EnumType.STRING)
    @Column(name = "cropCategory", nullable = false)
    private CropCategory cropCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "cropSeason", nullable = false)
    private CropSeason cropSeason;

    @Column(name = "typicalDurationDays", nullable = false)
    private Integer typicalDurationDays;

    @Column(name = "expectedYieldPerAcre", nullable = false, precision = 8, scale = 2)
    private BigDecimal expectedYieldPerAcre;

    @Enumerated(EnumType.STRING)
    @Column(name = "catalogStatus", nullable = false)
    private CatalogStatus catalogStatus = CatalogStatus.Ac;

    @OneToMany(mappedBy = "cropCatalog", fetch = FetchType.LAZY)
    private List<CropPlan> cropPlans;

    public CropCatalog() {}

    public Integer getCropId() { return cropId; }
    public void setCropId(Integer id) { this.cropId = id; }

    public String getCropName() { return cropName; }
    public void setCropName(String name) { this.cropName = name; }

    public CropCategory getCropCategory() { return cropCategory; }
    public void setCropCategory(CropCategory cat) { this.cropCategory = cat; }

    public CropSeason getCropSeason() { return cropSeason; }
    public void setCropSeason(CropSeason season) { this.cropSeason = season; }

    public Integer getTypicalDurationDays() { return typicalDurationDays; }
    public void setTypicalDurationDays(Integer days) { this.typicalDurationDays = days; }

    public BigDecimal getExpectedYieldPerAcre() { return expectedYieldPerAcre; }
    public void setExpectedYieldPerAcre(BigDecimal yield) { this.expectedYieldPerAcre = yield; }

    public CatalogStatus getCatalogStatus() { return catalogStatus; }
    public void setCatalogStatus(CatalogStatus status) { this.catalogStatus = status; }

    public List<CropPlan> getCropPlans() { return cropPlans; }
    public void setCropPlans(List<CropPlan> plans) { this.cropPlans = plans; }

    public enum CropCategory {
        Cereal, Pulse, Vegetable, Fruit, Cash, Oilseed, Spice, Fodder
    }
    public enum CropSeason {
        Kharif, Rabi, Zaid, Perennial
    }
    public enum CatalogStatus {
        Ac, In
    }
}
