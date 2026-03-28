package com.skeeterSoftworks.WorkOrderCentral.testdata;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ApplicationUser;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Customer;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Machine;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Tool;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.CustomerRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MachineRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ToolRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.UserRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.ERole;
import net.datafaker.Faker;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Seeds the configured database with sample rows (10 each: users, machines, tools, products, customers).
 * <p>
 * <b>How to run on demand</b>
 * <ul>
 *   <li>IntelliJ: remove {@code @Disabled} from {@link #generateSampleData()}, or use a run configuration
 *       that includes disabled tests.</li>
 *   <li>Maven: {@code mvn test -Dtest=ManualSampleDataGenerationTest -Djunit.jupiter.conditions.deactivate=org.junit.jupiter.api.condition.DisabledCondition}</li>
 * </ul>
 * Requires a running DB matching your Spring profile (e.g. {@code application-pg.properties}).
 * Running multiple times creates additional rows (not idempotent).
 * String fields use <a href="https://github.com/datafaker-net/datafaker">Datafaker</a> (Java Faker–compatible API).
 */
@SpringBootTest
class ManualSampleDataGenerationTest {

    private static final Logger log = LoggerFactory.getLogger(ManualSampleDataGenerationTest.class);

    private static final int SAMPLE_COUNT = 10;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("Generate sample users, machines, tools, products, customers (manual / disabled by default)")
 //   @Disabled("Remove @Disabled or deactivate DisabledCondition to insert sample data into the database.")
    void generateSampleData() {
        Faker faker = new Faker(Locale.ENGLISH);

        List<Machine> savedMachines = new ArrayList<>();
        for (int i = 1; i <= SAMPLE_COUNT; i++) {
            Machine m = new Machine();
            m.setMachineName(faker.company().name() + " " + faker.commerce().material() + " #" + i);
            m.setCycleTime(30L + faker.number().numberBetween(1, 120));
            m.setBarLocation(faker.address().cityName() + " — " + faker.address().streetName());
            m.setPiecesPerBar(5L + faker.number().numberBetween(1, 50));
            m.setBarsPerSeries(1L + faker.number().numberBetween(1, 10));
            m.setBarsCount(10L + faker.number().numberBetween(1, 40));
            m.setWeightPerBar(faker.number().randomDouble(2, 5, 80));
            m.setSumBarWeight(faker.number().randomDouble(2, 50, 500));
            m.setSeriesID("SER-" + faker.regexify("[A-Z]{2}\\d{4}") + "-" + i);
            savedMachines.add(machineRepository.save(m));
        }

        List<Tool> savedTools = new ArrayList<>();
        for (int i = 0; i < SAMPLE_COUNT; i++) {
            Tool t = new Tool();
            t.setToolName(faker.commerce().productName() + " tool");
            t.setToolDescription(faker.lorem().sentence(faker.number().numberBetween(8, 16)));
            t.setMachine(savedMachines.get(i));
            savedTools.add(toolRepository.save(t));
        }

        byte[] sampleTechnicalDrawing;
        try {
            sampleTechnicalDrawing = new ClassPathResource("sample-technical-drawing.jpg")
                    .getInputStream()
                    .readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("classpath:sample-technical-drawing.jpg", e);
        }

        for (int i = 0; i < SAMPLE_COUNT; i++) {
            Product p = new Product();
            p.setName(faker.commerce().productName());
            p.setReference("REF-" + faker.regexify("[A-Z0-9]{6}") + "-" + (i + 1));
            p.setDescription(faker.lorem().paragraph(1));
            p.setProductGroup(faker.commerce().department());
            p.setStockQuantity((long) faker.number().numberBetween(0, 10_000));
            p.setTechnicalDrawing(sampleTechnicalDrawing);
            p.setTool(savedTools.get(i));
            p.getMachines().add(savedMachines.get(i));
            productRepository.save(p);
        }

        for (int i = 1; i <= SAMPLE_COUNT; i++) {
            Customer c = new Customer();
            c.setCompanyName(faker.company().name() + " " + faker.company().suffix());
            c.setAddressData(faker.address().fullAddress());
            c.setDescription(faker.company().catchPhrase() + " — " + faker.lorem().sentence(4));
            customerRepository.save(c);
        }

        for (int i = 1; i <= SAMPLE_COUNT; i++) {
            ApplicationUser u = new ApplicationUser();
            u.setName(faker.name().firstName());
            u.setSurname(faker.name().lastName());
            u.setQrCode("QR-" + faker.regexify("[A-Z0-9]{10}") + "-" + i);
            u.setRole(i % 2 == 0 ? ERole.ADMIN : ERole.OPERATOR);
            u.setCreatedDate(LocalDateTime.now());
            userRepository.save(u);
        }

        log.info(
                "Sample data inserted: {} machines, {} tools, {} products, {} customers, {} users",
                savedMachines.size(),
                savedTools.size(),
                SAMPLE_COUNT,
                SAMPLE_COUNT,
                SAMPLE_COUNT
        );
    }
}
