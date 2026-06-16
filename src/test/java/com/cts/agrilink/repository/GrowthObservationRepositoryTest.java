package com.cts.agrilink.repository;

import com.cts.agrilink.model.CropCatalog;
import com.cts.agrilink.model.CropPlan;
import com.cts.agrilink.model.GrowthObservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = "spring.jpa.properties.hibernate.dialect=")
class GrowthObservationRepositoryTest {

    @Autowired
    private GrowthObservationRepository growthObservationRepository;

    @Autowired
    private CropPlanRepository cropPlanRepository;

    @Autowired
    private CropCatalogRepository cropCatalogRepository;

    private CropPlan planA;
    private CropPlan planB;

    private GrowthObservation obs1;
    private GrowthObservation obs2;
    private GrowthObservation obs3;
    private GrowthObservation obs4;

    @BeforeEach
    void setUp() {
        growthObservationRepository.deleteAll();
        cropPlanRepository.deleteAll();
        cropCatalogRepository.deleteAll();

        CropCatalog rice = new CropCatalog();
        rice.setCropName("Rice");
        rice.setCropCategory(CropCatalog.CropCategory.Cereal);
        rice.setCropSeason(CropCatalog.CropSeason.Kharif);
        rice.setTypicalDurationDays(120);
        rice.setExpectedYieldPerAcre(new BigDecimal("25.00"));
        cropCatalogRepository.save(rice);

        planA = makePlan(1L, 101L, rice, CropPlan.PlanSeason.Kharif, 2026, CropPlan.PlanStatus.Pl);
        planB = makePlan(2L, 202L, rice, CropPlan.PlanSeason.Rabi,   2026, CropPlan.PlanStatus.So);
        cropPlanRepository.saveAll(List.of(planA, planB));

        obs1 = makeObs(planA, 10, LocalDate.of(2026, 7, 1),  GrowthObservation.GrowthStage.Germination, false, "Good germination");
        obs2 = makeObs(planA, 10, LocalDate.of(2026, 7, 15), GrowthObservation.GrowthStage.Vegetative,  true,  "Aphids spotted");
        obs3 = makeObs(planB, 20, LocalDate.of(2026, 8, 1),  GrowthObservation.GrowthStage.Flowering,   true,  "Fungal infection");
        obs4 = makeObs(planB, 20, LocalDate.of(2026, 9, 1),  GrowthObservation.GrowthStage.Maturity,    false, "Crop maturing well");
        growthObservationRepository.saveAll(List.of(obs1, obs2, obs3, obs4));
    }

    private CropPlan makePlan(Long farmerId, Long holdingId, CropCatalog crop,
                               CropPlan.PlanSeason season, int year, CropPlan.PlanStatus status) {
        CropPlan p = new CropPlan();
        p.setFarmerId(farmerId);
        p.setHoldingId(holdingId);
        p.setCropCatalog(crop);
        p.setPlanSeason(season);
        p.setPlanYear(year);
        p.setSowingDate(LocalDate.of(year, 6, 1));
        p.setExpectedHarvestDate(LocalDate.of(year, 10, 1));
        p.setAreaPlanted(new BigDecimal("3.00"));
        p.setPlanStatus(status);
        return p;
    }

    private GrowthObservation makeObs(CropPlan plan, int officerId, LocalDate date,
                                       GrowthObservation.GrowthStage stage, boolean pest, String remarks) {
        GrowthObservation g = new GrowthObservation();
        g.setCropPlan(plan);
        g.setOfficerId(officerId);
        g.setObservationDate(date);
        g.setGrowthStage(stage);
        g.setPestOrDiseaseFlag(pest);
        g.setFieldRemarks(remarks);
        return g;
    }

    // Test 1
    @Test
    void save_persistsObservationAndGeneratesId() {
        GrowthObservation newObs = makeObs(planA, 30, LocalDate.of(2026, 8, 10),
                GrowthObservation.GrowthStage.Tillering, false, "Tillering stage");
        GrowthObservation saved = growthObservationRepository.save(newObs);

        assertThat(saved.getObservationId()).isNotNull();
        assertThat(saved.getGrowthStage()).isEqualTo(GrowthObservation.GrowthStage.Tillering);
    }

    // Test 2
    @Test
    void findById_returnsCorrectObservation() {
        Optional<GrowthObservation> found = growthObservationRepository.findById(obs1.getObservationId());

        assertThat(found).isPresent();
        assertThat(found.get().getOfficerId()).isEqualTo(10);
        assertThat(found.get().getGrowthStage()).isEqualTo(GrowthObservation.GrowthStage.Germination);
    }

    // Test 3
    @Test
    void findById_returnsEmpty_whenIdNotExists() {
        assertThat(growthObservationRepository.findById(99999)).isEmpty();
    }

    // Test 4
    @Test
    void findAll_returnsAllObservations() {
        assertThat(growthObservationRepository.findAll()).hasSize(4);
    }

    // Test 5
    @Test
    void delete_removesObservationFromDatabase() {
        growthObservationRepository.deleteById(obs1.getObservationId());

        assertThat(growthObservationRepository.findById(obs1.getObservationId())).isEmpty();
        assertThat(growthObservationRepository.count()).isEqualTo(3);
    }

    // Test 6
    @Test
    void update_changesFieldsCorrectly() {
        obs1.setFieldRemarks("Updated remark");
        obs1.setPestOrDiseaseFlag(true);
        growthObservationRepository.save(obs1);

        GrowthObservation updated = growthObservationRepository.findById(obs1.getObservationId()).get();
        assertThat(updated.getFieldRemarks()).isEqualTo("Updated remark");
        assertThat(updated.getPestOrDiseaseFlag()).isTrue();
    }

    // Test 7
    @Test
    void findByCropPlan_PlanId_returnsAllObservationsForPlan() {
        List<GrowthObservation> results = growthObservationRepository.findByCropPlan_PlanId(planA.getPlanId());

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(g -> g.getCropPlan().getPlanId().equals(planA.getPlanId()));
    }

    // Test 8
    @Test
    void findByCropPlan_PlanId_returnsEmptyList_whenNoObservationsExist() {
        CropCatalog wheat = new CropCatalog();
        wheat.setCropName("Wheat");
        wheat.setCropCategory(CropCatalog.CropCategory.Cereal);
        wheat.setCropSeason(CropCatalog.CropSeason.Rabi);
        wheat.setTypicalDurationDays(110);
        wheat.setExpectedYieldPerAcre(new BigDecimal("20.00"));
        cropCatalogRepository.save(wheat);

        CropPlan emptyPlan = makePlan(5L, 505L, wheat, CropPlan.PlanSeason.Rabi, 2027, CropPlan.PlanStatus.Pl);
        cropPlanRepository.save(emptyPlan);

        assertThat(growthObservationRepository.findByCropPlan_PlanId(emptyPlan.getPlanId())).isEmpty();
    }

    // Test 9
    @Test
    void findByPestOrDiseaseFlag_returnsOnlyFlaggedObservations() {
        List<GrowthObservation> flagged = growthObservationRepository.findByPestOrDiseaseFlag(true);

        assertThat(flagged).hasSize(2);
        assertThat(flagged).allMatch(GrowthObservation::getPestOrDiseaseFlag);
    }

    // Test 10
    @Test
    void findByPestOrDiseaseFlag_returnsOnlyCleanObservations() {
        List<GrowthObservation> clean = growthObservationRepository.findByPestOrDiseaseFlag(false);

        assertThat(clean).hasSize(2);
        assertThat(clean).noneMatch(GrowthObservation::getPestOrDiseaseFlag);
    }
}
