package com.cts.agrilink.performance;

import com.cts.agrilink.model.CropCatalog;
import com.cts.agrilink.model.CropPlan;
import net.datafaker.Faker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates realistic {@link CropPlan} test data using Java Faker (net.datafaker).
 *
 * <p>Kept in a separate class so the data-generation logic is reusable and the
 * performance test itself stays focused on measuring, not on building objects.</p>
 */
public final class CropPlanTestDataFactory {

    private final Faker faker = new Faker();

    private static final CropPlan.PlanSeason[] SEASONS = CropPlan.PlanSeason.values();

    /**
     * Build a single, valid CropPlan attached to the given crop.
     *
     * @param crop      a persisted CropCatalog row (the required FK)
     * @param uniqueSeq a monotonically increasing sequence used to guarantee the
     *                  (farmerId, holdingId, cropId, season, year) tuple is unique,
     *                  so the data is safe even against the service-layer dedup rule.
     */
    public CropPlan newPlan(CropCatalog crop, int uniqueSeq) {
        CropPlan p = new CropPlan();

        // uniqueSeq guarantees no duplicate business key collisions across 1000 rows
        p.setFarmerId((long) uniqueSeq);
        p.setHoldingId(faker.number().numberBetween(1L, 500_000L));
        p.setCropCatalog(crop);
        p.setPlanSeason(faker.options().option(SEASONS));
        p.setPlanYear(faker.number().numberBetween(2020, 2030));

        // expectedHarvestDate must be strictly after sowingDate (business rule)
        LocalDate sowing = LocalDate.of(2026, 1, 1)
                .plusDays(faker.number().numberBetween(0, 300));
        p.setSowingDate(sowing);
        p.setExpectedHarvestDate(sowing.plusDays(faker.number().numberBetween(60, 180)));

        BigDecimal area = BigDecimal.valueOf(faker.number().randomDouble(2, 1, 500))
                .setScale(2, RoundingMode.HALF_UP);
        p.setAreaPlanted(area);

        p.setPlanStatus(CropPlan.PlanStatus.Pl);
        return p;
    }

    /**
     * Build {@code count} CropPlans, distributing them evenly across the supplied crops.
     */
    public List<CropPlan> newPlans(List<CropCatalog> crops, int count) {
        List<CropPlan> plans = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            CropCatalog crop = crops.get(i % crops.size());
            plans.add(newPlan(crop, i + 1));
        }
        return plans;
    }

    /** Build a CropCatalog seed row (needed because CropPlan.cropId is a NOT NULL FK). */
    public CropCatalog newCatalog() {
        CropCatalog c = new CropCatalog();
        c.setCropName(faker.food().vegetable() + "-" + faker.number().digits(4));
        c.setCropCategory(faker.options().option(CropCatalog.CropCategory.values()));
        c.setCropSeason(faker.options().option(CropCatalog.CropSeason.values()));
        c.setTypicalDurationDays(faker.number().numberBetween(60, 200));
        c.setExpectedYieldPerAcre(
                BigDecimal.valueOf(faker.number().randomDouble(2, 5, 60))
                        .setScale(2, RoundingMode.HALF_UP));
        c.setCatalogStatus(CropCatalog.CatalogStatus.Ac);
        return c;
    }
}
