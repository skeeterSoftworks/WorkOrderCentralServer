package com.skeeterSoftworks.WorkOrderCentral.report;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Customer;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.PurchaseOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrderStockAssignment;
import com.skeeterSoftworks.WorkOrderCentral.service.StockProductInventoryService;
import net.datafaker.Faker;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Generates a sample Stock Assignment Order PDF with faker data and writes it under {@code target/}
 * for visual review. No database required.
 * <p>
 * <b>How to run on demand</b>
 * <ul>
 *   <li>IntelliJ: remove {@link Disabled} from {@link #dumpSampleStockAssignmentOrderPdf()}, or use a run
 *       configuration that includes disabled tests.</li>
 *   <li>Maven: {@code mvn test -Dtest=ManualStockAssignmentOrderPdfGenerationTest -Djunit.jupiter.conditions.deactivate=org.junit.jupiter.api.condition.DisabledCondition}</li>
 * </ul>
 */
class ManualStockAssignmentOrderPdfGenerationTest {

    private static final Logger log = LoggerFactory.getLogger(ManualStockAssignmentOrderPdfGenerationTest.class);

    @Test
   // @Disabled("Remove @Disabled or deactivate DisabledCondition to write a sample PDF to target/ for visual review.")
    @DisplayName("Dump Stock Assignment Order PDF sample (manual / disabled by default)")
    void dumpSampleStockAssignmentOrderPdf() throws Exception {
        Faker faker = new Faker(new Locale("sr", "RS"));

        Customer customer = new Customer();
        customer.setId(42L);
        customer.setCompanyName(faker.company().name());

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setId(1001L);
        purchaseOrder.setCustomer(customer);

        Product product = new Product();
        product.setId(7L);
        product.setReference("PRD-" + faker.number().digits(4));
        product.setName(faker.commerce().productName());

        ProductOrder line = new ProductOrder();
        line.setId(501L);
        line.setQuantity(faker.number().numberBetween(10, 200));
        line.setProduct(product);
        line.setPurchaseOrder(purchaseOrder);

        WorkOrder workOrder = new WorkOrder();
        workOrder.setId(9001L);
        workOrder.setProductOrder(line);

        int assignedQty = faker.number().numberBetween(1, Math.max(1, line.getQuantity()));
        WorkOrderStockAssignment assignment = new WorkOrderStockAssignment();
        assignment.setWorkOrder(workOrder);
        assignment.setProduct(product);
        assignment.setQuantity(assignedQty);
        assignment.setAssignedAt(LocalDateTime.now());

        String createdByFullName = "Marko Živković (Čč Žž Šš Đđ)";

        StockAssignmentReportLocale locale = new StockAssignmentReportLocale();
        ReflectionTestUtils.setField(locale, "configuredLocale", "sr");

        StockProductInventoryService service = new StockProductInventoryService(
                null, null, null, null, null, null, locale);

        String pdfBase64 = service.generateStockAssignmentOrderPdfBase64(
                workOrder,
                List.of(assignment),
                createdByFullName);

        byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);
        assertTrue(pdfBytes.length > 100, "PDF should contain non-trivial content");

        Path output = Path.of("target", "stock-assignment-order-sample.pdf");
        Files.createDirectories(output.getParent());
        Files.write(output, pdfBytes);

        log.info("Stock Assignment Order sample PDF written to {}", output.toAbsolutePath());
        log.info("Created by (faker): {}", createdByFullName);
    }
}
