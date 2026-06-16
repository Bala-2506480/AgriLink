package com.cts.agrilink.repository;

import com.cts.agrilink.model.CropCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = "spring.jpa.properties.hibernate.dialect=")
class CropCatalogRepositoryTest {

    @Autowired
    private CropCatalogRepository cropCatalogRepository;

    private CropCatalog rice;
    private CropCatalog wheat;
    private CropCatalog tomato;

    @BeforeEach
    void setUp() {
        cropCatalogRepository.deleteAll();

        rice = new CropCatalog();
        rice.setCropName("Rice");
        rice.setCropCategory(CropCatalog.CropCategory.Cereal);
        rice.setCropSeason(CropCatalog.CropSeason.Kharif);
        rice.setTypicalDurationDays(120);
        rice.setExpectedYieldPerAcre(new BigDecimal("25.00"));
        rice.setCatalogStatus(CropCatalog.CatalogStatus.Ac);

        wheat = new CropCatalog();
        wheat.setCropName("Wheat");
        wheat.setCropCategory(CropCatalog.CropCategory.Cereal);
        wheat.setCropSeason(CropCatalog.CropSeason.Rabi);
        wheat.setTypicalDurationDays(110);
        wheat.setExpectedYieldPerAcre(new BigDecimal("20.00"));
        wheat.setCatalogStatus(CropCatalog.CatalogStatus.Ac);

        tomato = new CropCatalog();
        tomato.setCropName("Tomato");
        tomato.setCropCategory(CropCatalog.CropCategory.Vegetable);
        tomato.setCropSeason(CropCatalog.CropSeason.Zaid);
        tomato.setTypicalDurationDays(70);
        tomato.setExpectedYieldPerAcre(new BigDecimal("30.00"));
        tomato.setCatalogStatus(CropCatalog.CatalogStatus.In);

        cropCatalogRepository.saveAll(List.of(rice, wheat, tomato));
    }

    // Test 1
    @Test
    void save_persistsCropAndGeneratesId() {
        CropCatalog mango = new CropCatalog();
        mango.setCropName("Mango");
        mango.setCropCategory(CropCatalog.CropCategory.Fruit);
        mango.setCropSeason(CropCatalog.CropSeason.Perennial);
        mango.setTypicalDurationDays(365);
        mango.setExpectedYieldPerAcre(new BigDecimal("15.00"));

        CropCatalog saved = cropCatalogRepository.save(mango);

        assertThat(saved.getCropId()).isNotNull();
        assertThat(saved.getCropName()).isEqualTo("Mango");
    }

    // Test 2
    @Test
    void findById_returnsCorrectCrop() {
        Optional<CropCatalog> found = cropCatalogRepository.findById(rice.getCropId());

        assertThat(found).isPresent();
        assertThat(found.get().getCropName()).isEqualTo("Rice");
    }

    // Test 3
    @Test
    void findById_returnsEmpty_whenIdNotExists() {
        assertThat(cropCatalogRepository.findById(99999)).isEmpty();
    }

    // Test 4
    @Test
    void findAll_returnsAllCrops() {
        assertThat(cropCatalogRepository.findAll()).hasSize(3);
    }

    // Test 5
    @Test
    void delete_removesCropFromDatabase() {
        cropCatalogRepository.deleteById(rice.getCropId());

        assertThat(cropCatalogRepository.findById(rice.getCropId())).isEmpty();
        assertThat(cropCatalogRepository.count()).isEqualTo(2);
    }

    // Test 6
    @Test
    void update_changesFieldCorrectly() {
        rice.setTypicalDurationDays(130);
        cropCatalogRepository.save(rice);

        assertThat(cropCatalogRepository.findById(rice.getCropId()).get().getTypicalDurationDays())
                .isEqualTo(130);
    }

    // Test 7
    @Test
    void findByCropSeason_returnsMatchingCrops() {
        List<CropCatalog> kharif = cropCatalogRepository.findByCropSeason(CropCatalog.CropSeason.Kharif);

        assertThat(kharif).hasSize(1);
        assertThat(kharif.get(0).getCropName()).isEqualTo("Rice");
    }

    // Test 8
    @Test
    void findByCropSeason_returnsEmptyList_whenNoMatch() {
        assertThat(cropCatalogRepository.findByCropSeason(CropCatalog.CropSeason.Perennial)).isEmpty();
    }

    // Test 9
    @Test
    void findByCropSeason_returnsMultiple_whenSameSeasonExists() {
        CropCatalog barley = new CropCatalog();
        barley.setCropName("Barley");
        barley.setCropCategory(CropCatalog.CropCategory.Cereal);
        barley.setCropSeason(CropCatalog.CropSeason.Rabi);
        barley.setTypicalDurationDays(100);
        barley.setExpectedYieldPerAcre(new BigDecimal("18.00"));
        cropCatalogRepository.save(barley);

        assertThat(cropCatalogRepository.findByCropSeason(CropCatalog.CropSeason.Rabi)).hasSize(2);
    }

    // Test 10
    @Test
    void findByCropCategory_returnsMatchingCrops() {
        List<CropCatalog> cereals = cropCatalogRepository.findByCropCategory(CropCatalog.CropCategory.Cereal);

        assertThat(cereals).hasSize(2);
        assertThat(cereals).extracting(CropCatalog::getCropName)
                .containsExactlyInAnyOrder("Rice", "Wheat");
    }

    // Test 11
    @Test
    void findByCropCategory_returnsEmptyList_whenNoMatch() {
        assertThat(cropCatalogRepository.findByCropCategory(CropCatalog.CropCategory.Spice)).isEmpty();
    }

    // Test 12
    @Test
    void findByCropCategory_returnsSingleCrop() {
        List<CropCatalog> vegetables = cropCatalogRepository.findByCropCategory(CropCatalog.CropCategory.Vegetable);

        assertThat(vegetables).hasSize(1);
        assertThat(vegetables.get(0).getCropName()).isEqualTo("Tomato");
    }
}
