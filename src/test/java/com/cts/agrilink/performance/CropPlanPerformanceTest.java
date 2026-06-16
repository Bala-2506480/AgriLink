package com.cts.agrilink.performance;

import com.cts.agrilink.model.CropCatalog;
import com.cts.agrilink.model.CropPlan;
import com.cts.agrilink.repository.CropCatalogRepository;
import com.cts.agrilink.repository.CropPlanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance / load test for the {@code crop_plan} table — the busiest write
 * path in AgriLink, since every farmer (the largest user group) creates crop
 * plans each season.
 *
 * <p>Generates <b>1000</b> rows with Java Faker (see {@link CropPlanTestDataFactory})
 * and measures three things:</p>
 * <ol>
 *   <li><b>Row-by-row insert</b> — simulates 1000 individual "create plan" calls.</li>
 *   <li><b>Bulk insert</b> — {@code saveAll} of 1000 rows in one batch.</li>
 *   <li><b>Read</b> — {@code findAll} over the populated table.</li>
 * </ol>
 *
 * <p>Runs against in-memory H2 by default (no MySQL needed, always green in CI).
 * To benchmark against real MySQL, see the header comment in the README / the
 * instructions returned with this file.</p>
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.dialect=",
        // show-sql floods the console and skews timings — silence it for perf runs
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.format_sql=false"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CropPlanPerformanceTest {

    private static final int RECORD_COUNT = 1000;

    /** Soft upper bound for 1000 single inserts on H2 (ms). Generous so CI is stable. */
    private static final long SINGLE_INSERT_BUDGET_MS = 20_000;

    @Autowired
    private CropPlanRepository cropPlanRepository;

    @Autowired
    private CropCatalogRepository cropCatalogRepository;

    private final CropPlanTestDataFactory factory = new CropPlanTestDataFactory();

    private List<CropCatalog> seedCatalog() {
        List<CropCatalog> crops = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            crops.add(factory.newCatalog());
        }
        return cropCatalogRepository.saveAll(crops);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Test 1 — 1000 individual inserts (the real "user creates a plan" path)
    // ──────────────────────────────────────────────────────────────────────
    @Test
    @Order(1)
    @DisplayName("Insert 1000 crop plans one-by-one and report latency")
    void singleInsertPerformance() {
        List<CropCatalog> crops = seedCatalog();
        List<CropPlan> plans = factory.newPlans(crops, RECORD_COUNT);

        List<Long> latenciesNs = new ArrayList<>(RECORD_COUNT);

        long start = System.nanoTime();
        for (CropPlan plan : plans) {
            long t0 = System.nanoTime();
            cropPlanRepository.save(plan);
            latenciesNs.add(System.nanoTime() - t0);
        }
        cropPlanRepository.flush(); // force pending SQL to the DB before stopping the clock
        long totalNs = System.nanoTime() - start;

        report("SINGLE INSERT (1000x save)", totalNs, latenciesNs);

        assertThat(cropPlanRepository.count()).isEqualTo(RECORD_COUNT);
        assertThat(totalNs / 1_000_000.0)
                .as("1000 single inserts should finish within the time budget")
                .isLessThan(SINGLE_INSERT_BUDGET_MS);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Test 2 — Bulk insert via saveAll (batch create)
    // ──────────────────────────────────────────────────────────────────────
    @Test
    @Order(2)
    @DisplayName("Bulk insert 1000 crop plans via saveAll and report throughput")
    void bulkInsertPerformance() {
        List<CropCatalog> crops = seedCatalog();
        List<CropPlan> plans = factory.newPlans(crops, RECORD_COUNT);

        long start = System.nanoTime();
        cropPlanRepository.saveAll(plans);
        cropPlanRepository.flush();
        long totalNs = System.nanoTime() - start;

        report("BULK INSERT (saveAll)", totalNs, Collections.emptyList());

        assertThat(cropPlanRepository.count()).isEqualTo(RECORD_COUNT);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Test 3 — Read performance over the populated table
    // ──────────────────────────────────────────────────────────────────────
    @Test
    @Order(3)
    @DisplayName("Read all 1000 crop plans and report query time")
    void readPerformance() {
        List<CropCatalog> crops = seedCatalog();
        cropPlanRepository.saveAll(factory.newPlans(crops, RECORD_COUNT));
        cropPlanRepository.flush();

        long start = System.nanoTime();
        List<CropPlan> all = cropPlanRepository.findAll();
        long totalNs = System.nanoTime() - start;

        report("READ (findAll of 1000 rows)", totalNs, Collections.emptyList());

        assertThat(all).hasSize(RECORD_COUNT);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Reporting helper — prints total, throughput and (for single inserts) percentiles
    // ──────────────────────────────────────────────────────────────────────
    private void report(String label, long totalNs, List<Long> latenciesNs) {
        double totalMs = totalNs / 1_000_000.0;
        double throughput = RECORD_COUNT / (totalNs / 1_000_000_000.0);

        StringBuilder sb = new StringBuilder();
        sb.append("\n==================== ").append(label).append(" ====================\n");
        sb.append(String.format("Records          : %d%n", RECORD_COUNT));
        sb.append(String.format("Total time       : %.2f ms%n", totalMs));
        sb.append(String.format("Avg per record   : %.3f ms%n", totalMs / RECORD_COUNT));
        sb.append(String.format("Throughput       : %.0f records/sec%n", throughput));

        if (!latenciesNs.isEmpty()) {
            List<Long> sorted = latenciesNs.stream().sorted().collect(Collectors.toList());
            sb.append(String.format("Min latency      : %.3f ms%n", ms(sorted.get(0))));
            sb.append(String.format("p50 latency      : %.3f ms%n", ms(percentile(sorted, 50))));
            sb.append(String.format("p95 latency      : %.3f ms%n", ms(percentile(sorted, 95))));
            sb.append(String.format("p99 latency      : %.3f ms%n", ms(percentile(sorted, 99))));
            sb.append(String.format("Max latency      : %.3f ms%n", ms(sorted.get(sorted.size() - 1))));
        }
        sb.append("=".repeat(58)).append("\n");
        System.out.println(sb);
    }

    private static double ms(long ns) {
        return ns / 1_000_000.0;
    }

    private static long percentile(List<Long> sorted, int p) {
        int idx = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(idx, sorted.size() - 1)));
    }
}
