package com.skeeterSoftworks.WorkOrderCentral.testdata;

import com.skeeterSoftworks.WorkOrderCentral.service.SampleDataGenerationService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.SampleDataGenerationResultTO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Seeds the configured database with sample rows (10 each: users, machines, tools, products, customers).
 * <p>
 * Delegates to {@link SampleDataGenerationService} (same logic as the admin UI “Generate test data” action).
 * <p>
 * <b>How to run on demand</b>
 * <ul>
 *   <li>IntelliJ: remove {@code @Disabled} from {@link #generateSampleData()}, or use a run configuration
 *       that includes disabled tests.</li>
 *   <li>Maven: {@code mvn test -Dtest=ManualSampleDataGenerationTest -Djunit.jupiter.conditions.deactivate=org.junit.jupiter.api.condition.DisabledCondition}</li>
 * </ul>
 * Requires a running DB matching your Spring profile (e.g. {@code application-pg.properties}).
 * Running multiple times creates additional rows (not idempotent).
 */
@SpringBootTest
class ManualSampleDataGenerationTest {

    private static final Logger log = LoggerFactory.getLogger(ManualSampleDataGenerationTest.class);

    @Autowired
    private SampleDataGenerationService sampleDataGenerationService;

    @Test
    @DisplayName("Generate sample users, machines, tools, products, customers (manual / disabled by default)")
    @Disabled("Remove @Disabled or deactivate DisabledCondition to insert sample data into the database.")
    void generateSampleData() {
        SampleDataGenerationResultTO result = sampleDataGenerationService.generateDemoBatch();
        log.info(
                "Sample data inserted: {} machines, {} tools, {} products, {} customers, {} users",
                result.getMachinesInserted(),
                result.getToolsInserted(),
                result.getProductsInserted(),
                result.getCustomersInserted(),
                result.getUsersInserted());
    }
}
