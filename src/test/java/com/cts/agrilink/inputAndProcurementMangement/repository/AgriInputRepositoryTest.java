package com.cts.agrilink.inputAndProcurementMangement.repository;

import com.cts.agrilink.inputAndProcurementMangement.model.AgriInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AgriInputRepositoryTest {

    @Autowired
    private AgriInputRepository agriInputRepository;

    @BeforeEach
    void setUp() {
        agriInputRepository.deleteAll();

        agriInputRepository.save(AgriInput.builder()
                .name("Urea").category("Fertiliser").unit("Kg")
                .pricePerUnit(20.0).subsidisedPrice(15.0).availableStock(500).status("Available")
                .build());

        agriInputRepository.save(AgriInput.builder()
                .name("DAP").category("Fertiliser").unit("Kg")
                .pricePerUnit(35.0).subsidisedPrice(28.0).availableStock(0).status("OutOfStock")
                .build());

        agriInputRepository.save(AgriInput.builder()
                .name("Wheat Seeds").category("Seed").unit("Kg")
                .pricePerUnit(50.0).subsidisedPrice(40.0).availableStock(300).status("Available")
                .build());

        agriInputRepository.save(AgriInput.builder()
                .name("Chlorpyrifos").category("Pesticide").unit("Litre")
                .pricePerUnit(120.0).subsidisedPrice(100.0).availableStock(150).status("Available")
                .build());
    }

    // ─── findAll ──────────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsAllSavedInputs() {
        List<AgriInput> inputs = agriInputRepository.findAll();

        assertThat(inputs).hasSize(4);
    }

    // ─── findById ─────────────────────────────────────────────────────────────────

    @Test
    void findById_existingId_returnsInput() {
        AgriInput saved = agriInputRepository.save(AgriInput.builder()
                .name("Tractor").category("Equipment").unit("Unit")
                .pricePerUnit(500000.0).subsidisedPrice(450000.0).availableStock(5).status("Available")
                .build());

        Optional<AgriInput> found = agriInputRepository.findById(saved.getInputId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Tractor");
    }

    @Test
    void findById_nonExistingId_returnsEmpty() {
        Optional<AgriInput> found = agriInputRepository.findById(9999L);

        assertThat(found).isEmpty();
    }

    // ─── findByCategory ───────────────────────────────────────────────────────────

    @Test
    void findByCategory_fertiliser_returnsOnlyFertiliserInputs() {
        List<AgriInput> result = agriInputRepository.findByCategoryIgnoreCase("Fertiliser");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(i -> "Fertiliser".equals(i.getCategory()));
    }

    @Test
    void findByCategory_seed_returnsOnlySeedInputs() {
        List<AgriInput> result = agriInputRepository.findByCategoryIgnoreCase("Seed");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Wheat Seeds");
    }

    @Test
    void findByCategory_noMatch_returnsEmptyList() {
        List<AgriInput> result = agriInputRepository.findByCategoryIgnoreCase("Equipment");

        assertThat(result).isEmpty();
    }

    // ─── findByStatus ─────────────────────────────────────────────────────────────

    @Test
    void findByStatus_available_returnsAvailableInputs() {
        List<AgriInput> result = agriInputRepository.findByStatusIgnoreCase("Available");

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(i -> "Available".equals(i.getStatus()));
    }

    @Test
    void findByStatus_outOfStock_returnsOutOfStockInputs() {
        List<AgriInput> result = agriInputRepository.findByStatusIgnoreCase("OutOfStock");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("DAP");
    }

    @Test
    void findByStatus_noMatch_returnsEmptyList() {
        List<AgriInput> result = agriInputRepository.findByStatusIgnoreCase("Discontinued");

        assertThat(result).isEmpty();
    }

    // ─── save ─────────────────────────────────────────────────────────────────────

    @Test
    void save_newInput_persistsAndGeneratesId() {
        AgriInput input = AgriInput.builder()
                .name("NPK Mix").category("Fertiliser").unit("Kg")
                .pricePerUnit(45.0).subsidisedPrice(38.0).availableStock(200).status("Available")
                .build();

        AgriInput saved = agriInputRepository.save(input);

        assertThat(saved.getInputId()).isNotNull();
        assertThat(agriInputRepository.findAll()).hasSize(5);
    }

    @Test
    void save_updatesExistingInput() {
        AgriInput input = agriInputRepository.findByCategoryIgnoreCase("Seed").get(0);
        input.setAvailableStock(0);
        input.setStatus("OutOfStock");

        agriInputRepository.save(input);

        AgriInput updated = agriInputRepository.findById(input.getInputId()).orElseThrow();
        assertThat(updated.getAvailableStock()).isZero();
        assertThat(updated.getStatus()).isEqualTo("OutOfStock");
    }

    // ─── existsById ───────────────────────────────────────────────────────────────

    @Test
    void existsById_existingId_returnsTrue() {
        Long id = agriInputRepository.findAll().get(0).getInputId();

        assertThat(agriInputRepository.existsById(id)).isTrue();
    }

    @Test
    void existsById_nonExistingId_returnsFalse() {
        assertThat(agriInputRepository.existsById(9999L)).isFalse();
    }

    // ─── deleteById ───────────────────────────────────────────────────────────────

    @Test
    void deleteById_existingId_removesRecord() {
        Long id = agriInputRepository.findAll().get(0).getInputId();

        agriInputRepository.deleteById(id);

        assertThat(agriInputRepository.findById(id)).isEmpty();
        assertThat(agriInputRepository.findAll()).hasSize(3);
    }
}
