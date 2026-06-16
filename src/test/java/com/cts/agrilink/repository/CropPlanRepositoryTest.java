package com.cts.agrilink.repository;

import com.cts.agrilink.model.CropCatalog;
import com.cts.agrilink.model.CropPlan;
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
class CropPlanRepositoryTest {

    @Autowired
    private CropPlanRepository cropPlanRepository;

    @Autowired
    private CropCatalogRepository cropCatalogRepository;

    private CropCatalog rice;
    private CropCatalog wheat;
    private CropPlan plan1;
    private CropPlan plan2;
    private CropPlan plan3;

    @BeforeEach
    void setUp() {
        cropPlanRepository.deleteAll();
        cropCatalogRepository.deleteAll();

        rice = new CropCatalog();
        rice.setCropName("Rice");
        rice.setCropCategory(CropCatalog.CropCategory.Cereal);
        rice.setCropSeason(CropCatalog.CropSeason.Kharif);
        rice.setTypicalDurationDays(120);
        rice.setExpectedYieldPerAcre(new BigDecimal("25.00"));
        cropCatalogRepository.save(rice);

        wheat = new CropCatalog();
        wheat.setCropName("Wheat");
        wheat.setCropCategory(CropCatalog.CropCategory.Cereal);
        wheat.setCropSeason(CropCatalog.CropSeason.Rabi);
        wheat.setTypicalDurationDays(110);
        wheat.setExpectedYieldPerAcre(new BigDecimal("20.00"));
        cropCatalogRepository.save(wheat);

        plan1 = makePlan(1L, 101L, rice,  CropPlan.PlanSeason.Kharif, 2026, CropPlan.PlanStatus.Pl);
        plan2 = makePlan(1L, 101L, wheat, CropPlan.PlanSeason.Rabi,   2026, CropPlan.PlanStatus.So);
        plan3 = makePlan(2L, 202L, rice,  CropPlan.PlanSeason.Kharif, 2026, CropPlan.PlanStatus.Gr);
        cropPlanRepository.saveAll(List.of(plan1, plan2, plan3));
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

    // Test 1
    @Test
    void save_persistsPlanAndGeneratesId() {
        CropPlan newPlan = makePlan(3L, 303L, rice, CropPlan.PlanSeason.Kharif, 2027, CropPlan.PlanStatus.Pl);
        CropPlan saved = cropPlanRepository.save(newPlan);

        assertThat(saved.getPlanId()).isNotNull();
        assertThat(saved.getFarmerId()).isEqualTo(3L);
    }

    // Test 2
    @Test
    void findById_returnsCorrectPlan() {
        Optional<CropPlan> found = cropPlanRepository.findById(plan1.getPlanId());

        assertThat(found).isPresent();
        assertThat(found.get().getFarmerId()).isEqualTo(1L);
        assertThat(found.get().getPlanStatus()).isEqualTo(CropPlan.PlanStatus.Pl);
    }

    // Test 3
    @Test
    void findById_returnsEmpty_whenIdNotExists() {
        assertThat(cropPlanRepository.findById(99999)).isEmpty();
    }

    // Test 4
    @Test
    void findAll_returnsAllPlans() {
        assertThat(cropPlanRepository.findAll()).hasSize(3);
    }

    // Test 5
    @Test
    void delete_removesPlanFromDatabase() {
        cropPlanRepository.deleteById(plan1.getPlanId());

        assertThat(cropPlanRepository.findById(plan1.getPlanId())).isEmpty();
        assertThat(cropPlanRepository.count()).isEqualTo(2);
    }

    // Test 6
    @Test
    void findByFarmerId_returnsAllPlansForFarmer() {
        List<CropPlan> plans = cropPlanRepository.findByFarmerId(1L);

        assertThat(plans).hasSize(2);
        assertThat(plans).allMatch(p -> p.getFarmerId().equals(1L));
    }

    // Test 7
    @Test
    void findByFarmerId_returnsSinglePlan() {
        List<CropPlan> plans = cropPlanRepository.findByFarmerId(2L);

        assertThat(plans).hasSize(1);
        assertThat(plans.get(0).getFarmerId()).isEqualTo(2L);
    }

    // Test 8
    @Test
    void findByFarmerId_returnsEmptyList_whenFarmerHasNoPlans() {
        assertThat(cropPlanRepository.findByFarmerId(999L)).isEmpty();
    }

    // Test 9
    @Test
    void findByPlanStatus_returnsPlannedPlans() {
        List<CropPlan> planned = cropPlanRepository.findByPlanStatus(CropPlan.PlanStatus.Pl);

        assertThat(planned).hasSize(1);
        assertThat(planned.get(0).getPlanStatus()).isEqualTo(CropPlan.PlanStatus.Pl);
    }

    // Test 10
    @Test
    void findByPlanStatus_returnsSownPlans() {
        List<CropPlan> sown = cropPlanRepository.findByPlanStatus(CropPlan.PlanStatus.So);

        assertThat(sown).hasSize(1);
        assertThat(sown.get(0).getFarmerId()).isEqualTo(1L);
    }
}
