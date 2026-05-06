package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ApplicationUser;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Customer;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Machine;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MeasuringFeaturePrototype;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.SetupDataPrototype;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Technology;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Tool;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.CustomerRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialProviderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MachineRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ToolRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.UserRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMeasureCheckType;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMeasuringFeatureClassType;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.ERole;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.SampleDataGenerationResultTO;
import net.datafaker.Faker;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Inserts the same demo batch as {@code ManualSampleDataGenerationTest} (10 rows per entity type).
 * Each product gets 3 demo {@link MeasuringFeaturePrototype}s, an embedded {@link SetupDataPrototype},
 * a {@link Technology} row (one-to-one), and one linked {@link Material}. Materials are linked to
 * demo {@link MaterialProvider}s.
 * Not idempotent: each call adds more rows.
 */
@Service
public class SampleDataGenerationService {

    public static final int SAMPLE_COUNT = 10;

    private final UserRepository userRepository;
    private final MachineRepository machineRepository;
    private final ToolRepository toolRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final MaterialRepository materialRepository;
    private final MaterialProviderRepository materialProviderRepository;
    private final CustomerService customerService;

    public SampleDataGenerationService(
            UserRepository userRepository,
            MachineRepository machineRepository,
            ToolRepository toolRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            MaterialRepository materialRepository,
            MaterialProviderRepository materialProviderRepository,
            CustomerService customerService) {
        this.userRepository = userRepository;
        this.machineRepository = machineRepository;
        this.toolRepository = toolRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.materialRepository = materialRepository;
        this.materialProviderRepository = materialProviderRepository;
        this.customerService = customerService;
    }

    @Transactional
    public SampleDataGenerationResultTO generateDemoBatch() {
        Faker faker = new Faker(Locale.ENGLISH);

        List<Machine> savedMachines = new ArrayList<>();
        for (int i = 1; i <= SAMPLE_COUNT; i++) {
            Machine m = new Machine();
            m.setMachineName(faker.company().name() + " " + faker.commerce().material() + " #" + i);
            m.setManufacturer(faker.company().name());
            m.setManufactureYear(faker.number().numberBetween(1995, 2025));
            m.setInternalNumber("INT-" + faker.regexify("[A-Z0-9]{6}") + "-" + i);
            m.setSerialNumber("SN-" + faker.regexify("[A-Z0-9]{8}") + "-" + i);
            m.setLocation(faker.address().cityName() + ", " + faker.address().streetAddress());
            savedMachines.add(machineRepository.save(m));
        }

        List<Tool> savedTools = new ArrayList<>();
        for (int i = 0; i < SAMPLE_COUNT; i++) {
            Tool t = new Tool();
            t.setToolName(faker.commerce().productName() + " tool");
            t.setToolDescription(faker.lorem().sentence(faker.number().numberBetween(8, 16)));
            t.setOrderNumber(i + 1);
            t.setWorkingTime(faker.number().numberBetween(5, 240));
            savedTools.add(toolRepository.save(t));
        }

        byte[] sampleTechnicalDrawing = loadSampleTechnicalDrawing();

        Customer internalStockOrderer = customerService.ensureInternalStockOrdererCustomerExists();

        List<Customer> savedCustomers = new ArrayList<>();
        for (int i = 1; i <= SAMPLE_COUNT; i++) {
            Customer c = new Customer();
            c.setCompanyName(faker.company().name() + " " + faker.company().suffix());
            c.setAddressData(faker.address().fullAddress());
            c.setDescription(faker.company().catchPhrase() + " — " + faker.lorem().sentence(4));
            savedCustomers.add(customerRepository.save(c));
        }

        List<MaterialProvider> savedProviders = new ArrayList<>();
        for (int i = 1; i <= SAMPLE_COUNT; i++) {
            MaterialProvider provider = new MaterialProvider();
            provider.setName(faker.company().name() + " Materials");
            provider.setContactPerson(faker.name().fullName());
            provider.setEmailAddress("supplier" + i + "@example.com");
            provider.setPhoneNumber("+3816" + faker.number().digits(7));
            provider.setGrade(faker.number().numberBetween(0, 6));
            savedProviders.add(materialProviderRepository.save(provider));
        }

        List<Material> savedMaterials = new ArrayList<>();
        for (int i = 1; i <= SAMPLE_COUNT; i++) {
            Material material = new Material();
            material.setName(faker.commerce().material());
            material.setCode("MAT-" + faker.regexify("[A-Z0-9]{6}") + "-" + i);
            material.setProductsPerUnit(faker.number().numberBetween(1, 101));
            material.setProvider(savedProviders.get(i - 1));
            material.setDiameter((float) faker.number().randomDouble(2, 1, 100));
            material.setWeight((float) faker.number().randomDouble(2, 1, 100));
            material.setLength((float) faker.number().randomDouble(2, 1, 100));
            material.setWidth((float) faker.number().randomDouble(2, 1, 100));
            savedMaterials.add(materialRepository.save(material));
        }

        for (int i = 0; i < SAMPLE_COUNT; i++) {
            Product p = new Product();
            p.setName(faker.commerce().productName());
            p.setReference("REF-" + faker.regexify("[A-Z0-9]{6}") + "-" + (i + 1));
            p.setDescription(faker.lorem().paragraph(1));
            p.setProductGroup(faker.commerce().department());
            p.setStockQuantity((long) faker.number().numberBetween(0, 10_000));
            p.setTechnicalDrawing(sampleTechnicalDrawing);
            p.getMachines().add(savedMachines.get(i));
            p.getCustomers().add(savedCustomers.get(i));
            p.getCustomers().add(internalStockOrderer);
            p.getMaterials().add(savedMaterials.get(i));
            addDemoMeasuringFeatures(p, i);
            Technology tech = buildDemoTechnology(faker, i);
            Tool toolForRow = savedTools.get(i);
            toolForRow.setTechnology(tech);
            tech.getTools().add(toolForRow);
            p.setTechnologyData(tech);
            p.setSetupDataPrototype(buildDemoSetupDataPrototype(i, toolForRow));
            productRepository.save(p);
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

        return new SampleDataGenerationResultTO(
                savedMachines.size(),
                savedTools.size(),
                SAMPLE_COUNT,
                SAMPLE_COUNT,
                SAMPLE_COUNT,
                savedMaterials.size(),
                savedProviders.size());
    }

    private static void addDemoMeasuringFeatures(Product product, int productIndex) {
        int n = productIndex + 1;
        String prefix = "DEMO-" + n + "-";

        MeasuringFeaturePrototype outerDiameter = new MeasuringFeaturePrototype();
        outerDiameter.setProduct(product);
        outerDiameter.setCatalogueId(prefix + "OD");
        outerDiameter.setDescription("Outside diameter — production control (measured)");
        outerDiameter.setRefValue(new BigDecimal("45.00000"));
        outerDiameter.setMinTolerance(new BigDecimal("44.95000"));
        outerDiameter.setMaxTolerance(new BigDecimal("45.05000"));
        outerDiameter.setClassType(EMeasuringFeatureClassType.CIC);
        outerDiameter.setFrequency("each part");
        outerDiameter.setCheckType(EMeasureCheckType.MEASURED);
        outerDiameter.setMeasuringTool("External micrometer 25-50 mm");
        product.getMeasuringFeaturePrototypes().add(outerDiameter);

        MeasuringFeaturePrototype length = new MeasuringFeaturePrototype();
        length.setProduct(product);
        length.setCatalogueId(prefix + "LEN");
        length.setDescription("Overall length (measured)");
        length.setRefValue(new BigDecimal("120.00000"));
        length.setMinTolerance(new BigDecimal("119.90000"));
        length.setMaxTolerance(new BigDecimal("120.10000"));
        length.setClassType(EMeasuringFeatureClassType.NORM);
        length.setFrequency("1 / 10");
        length.setCheckType(EMeasureCheckType.MEASURED);
        length.setMeasuringTool("Digital height gauge");
        product.getMeasuringFeaturePrototypes().add(length);

        MeasuringFeaturePrototype surface = new MeasuringFeaturePrototype();
        surface.setProduct(product);
        surface.setCatalogueId(prefix + "VIS");
        surface.setDescription("Surface / burr check (attributive OK–NOK)");
        surface.setRefValue(null);
        surface.setMinTolerance(null);
        surface.setMaxTolerance(null);
        surface.setClassType(EMeasuringFeatureClassType.CC);
        surface.setFrequency("spot check");
        surface.setCheckType(EMeasureCheckType.ATTRIBUTIVE);
        surface.setMeasuringTool("—");
        product.getMeasuringFeaturePrototypes().add(surface);
    }

    private static Technology buildDemoTechnology(Faker faker, int productIndex) {
        int n = productIndex + 1;
        Technology tech = new Technology();
        tech.setCycleTime(n + " s / " + faker.number().numberBetween(8, 95));
        tech.setNorm100(faker.number().numberBetween(90, 121));
        tech.setPiecesPerMaterial(faker.number().numberBetween(1, 24));
        return tech;
    }

    private static SetupDataPrototype buildDemoSetupDataPrototype(int productIndex, Tool assignedTool) {
        int n = productIndex + 1;
        SetupDataPrototype sd = new SetupDataPrototype();
        sd.setOperationID("DEMO-OP-" + n);
        sd.setToolID(assignedTool.getId() != null ? "TOOL-" + assignedTool.getId() : "DEMO-TOOL-" + n);
        sd.setDiameterRefValue(new BigDecimal("44.50000"));
        sd.setDiameterMaxNegTolerance(new BigDecimal("44.42000"));
        sd.setDiameterMaxPosTolerance(new BigDecimal("44.58000"));
        sd.setHeightRefValue(new BigDecimal("12.00000"));
        sd.setHeightMaxNegTolerance(new BigDecimal("11.96000"));
        sd.setHeightMaxPosTolerance(new BigDecimal("12.04000"));
        sd.setAttributiveHeightMeasurement(false);
        sd.setAttributiveDiameterMeasurement(false);
        return sd;
    }

    private static byte[] loadSampleTechnicalDrawing() {
        try {
            return new ClassPathResource("sample-technical-drawing.jpg").getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("classpath:sample-technical-drawing.jpg", e);
        }
    }
}
