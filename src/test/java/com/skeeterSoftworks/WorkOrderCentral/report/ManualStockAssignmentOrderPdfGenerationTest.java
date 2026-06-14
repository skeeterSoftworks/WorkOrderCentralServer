package com.skeeterSoftworks.WorkOrderCentral.report;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Customer;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.PurchaseOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockAssignmentOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.mapper.StockAssignmentOrderMapperService;
import com.skeeterSoftworks.WorkOrderCentral.service.StockProductInventoryService;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EStockAssignmentOrderStatus;
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
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Generates a sample Stock Assignment Order PDF with faker data and writes it under {@code target/}
 * for visual review. No database required.
 */
class ManualStockAssignmentOrderPdfGenerationTest {

    private static final Logger log = LoggerFactory.getLogger(ManualStockAssignmentOrderPdfGenerationTest.class);

    @Test
    @Disabled("Remove @Disabled or deactivate DisabledCondition to write a sample PDF to target/ for visual review.")
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
        StockAssignmentOrder order = new StockAssignmentOrder();
        order.setCode("12345678");
        order.setWorkOrder(workOrder);
        order.setProduct(product);
        order.setQuantity(assignedQty);
        order.setStatus(EStockAssignmentOrderStatus.UNASSIGNED);
        order.setCreatedAt(LocalDateTime.now());
        order.setCreatedByFullName("Marko Živković (Čč Žž Šš Đđ)");

        StockAssignmentReportLocale locale = new StockAssignmentReportLocale();
        ReflectionTestUtils.setField(locale, "configuredLocale", "sr");

        StockProductInventoryService service = new StockProductInventoryService(
                null, null, null, null, null, null, locale, new StockAssignmentOrderMapperService(), null);

        String pdfBase64 = service.generateStockAssignmentOrderPdfBase64(order);

        byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);
        assertTrue(pdfBytes.length > 100, "PDF should contain non-trivial content");

        Path output = Path.of("target", "stock-assignment-order-sample.pdf");
        Files.createDirectories(output.getParent());
        Files.write(output, pdfBytes);

        log.info("Stock Assignment Order sample PDF written to {}", output.toAbsolutePath());
        log.info("Order code: {}", order.getCode());
    }
}
