package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.PurchaseOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockedProduct;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrderStockAssignment;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.PurchaseOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.StockedProductRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.WorkOrderStockAssignmentRepository;
import com.skeeterSoftworks.WorkOrderCentral.report.StockAssignmentReportLine;
import com.skeeterSoftworks.WorkOrderCentral.report.StockAssignmentReportLocale;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductStockAvailabilityTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.WorkOrderStockAllocationTO;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockProductInventoryService {

    private final StockedProductRepository stockedProductRepository;
    private final ProductRepository productRepository;
    private final WorkOrderStockAssignmentRepository workOrderStockAssignmentRepository;
    private final StockService stockService;
    private final ProductOrderRepository productOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockAssignmentReportLocale stockAssignmentReportLocale;
    private volatile JasperReport compiledReport;

    public StockProductInventoryService(
            StockedProductRepository stockedProductRepository,
            ProductRepository productRepository,
            WorkOrderStockAssignmentRepository workOrderStockAssignmentRepository,
            StockService stockService,
            ProductOrderRepository productOrderRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            StockAssignmentReportLocale stockAssignmentReportLocale) {
        this.stockedProductRepository = stockedProductRepository;
        this.productRepository = productRepository;
        this.workOrderStockAssignmentRepository = workOrderStockAssignmentRepository;
        this.stockService = stockService;
        this.productOrderRepository = productOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.stockAssignmentReportLocale = stockAssignmentReportLocale;
    }

    @Transactional(readOnly = true)
    public ProductStockAvailabilityTO getAvailableProductStock(long productId) {
        if (productId <= 0) {
            return null;
        }
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return null;
        }
        long available = computeUnassignedAvailableQuantity(productId);
        return new ProductStockAvailabilityTO(
                productId,
                product.getReference(),
                product.getName(),
                available);
    }

    /**
     * Derives assignable finished-goods quantity without writing to {@code stocked_product}.
     */
    @Transactional(readOnly = true)
    public long computeUnassignedAvailableQuantity(long productId) {
        long aggregateAvailable = stockService.getAvailableQuantityForProduct(productId);
        long alreadyAssigned = workOrderStockAssignmentRepository.sumAssignedQuantityByProductId(productId);
        return Math.max(0, aggregateAvailable - alreadyAssigned);
    }

    @Transactional
    public void ensureStockedProductsSyncedForProduct(long productId) {
        long aggregateAvailable = stockService.getAvailableQuantityForProduct(productId);
        long alreadyAssigned = workOrderStockAssignmentRepository.sumAssignedQuantityByProductId(productId);
        long unassignedAvailable = Math.max(0, aggregateAvailable - alreadyAssigned);
        long storedTotal = stockedProductRepository.sumQuantityByProductId(productId);
        if (unassignedAvailable <= storedTotal) {
            return;
        }
        int delta = (int) Math.min(Integer.MAX_VALUE, unassignedAvailable - storedTotal);
        if (delta <= 0) {
            return;
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalStateException("PRODUCT_NOT_FOUND"));
        creditProduct(product, delta);
    }

    @Transactional
    public List<WorkOrderStockAssignment> applyWorkOrderStockAssignments(
            WorkOrder workOrder,
            List<WorkOrderStockAllocationTO> allocations) throws Exception {
        if (allocations == null || allocations.isEmpty()) {
            return List.of();
        }
        ProductOrder line = workOrder.getProductOrder();
        if (line == null || line.getProduct() == null || line.getProduct().getId() == null) {
            throw new Exception("WORK_ORDER_PRODUCT_REQUIRED");
        }
        long productId = line.getProduct().getId();

        int assignedTotal = sumAllocationQuantities(allocations);
        if (assignedTotal <= 0) {
            throw new Exception("WORK_ORDER_STOCK_ASSIGNMENT_QUANTITY_INVALID");
        }
        long available = computeUnassignedAvailableQuantity(productId);
        if (assignedTotal > available) {
            throw new Exception("WORK_ORDER_STOCK_ASSIGNMENT_INSUFFICIENT_QUANTITY");
        }

        int requiredLineQty = line.getQuantity() > 0 ? line.getQuantity() : Integer.MAX_VALUE;
        if (assignedTotal > requiredLineQty) {
            throw new Exception("WORK_ORDER_STOCK_ASSIGNMENT_EXCEEDS_REQUIRED");
        }

        ensureStockedProductsSyncedForProduct(productId);

        StockedProduct stocked = stockedProductRepository.findByProduct_Id(productId)
                .orElseThrow(() -> new Exception("WORK_ORDER_STOCK_ASSIGNMENT_UNAVAILABLE"));
        if (stocked.getQuantity() < assignedTotal) {
            throw new Exception("WORK_ORDER_STOCK_ASSIGNMENT_INSUFFICIENT_QUANTITY");
        }
        stocked.setQuantity(stocked.getQuantity() - assignedTotal);
        stockedProductRepository.save(stocked);

        Product product = line.getProduct();
        LocalDateTime assignedAt = LocalDateTime.now();
        WorkOrderStockAssignment assignment = new WorkOrderStockAssignment();
        assignment.setWorkOrder(workOrder);
        assignment.setProduct(product);
        assignment.setQuantity(assignedTotal);
        assignment.setAssignedAt(assignedAt);
        return List.of(workOrderStockAssignmentRepository.save(assignment));
    }

    public String generateStockAssignmentOrderPdfBase64(
            WorkOrder workOrder,
            List<WorkOrderStockAssignment> assignments,
            String createdByFullName) throws Exception {
        if (assignments == null || assignments.isEmpty()) {
            return null;
        }
        ProductOrder line = workOrder.getProductOrder();
        Product product = line != null ? line.getProduct() : null;
        PurchaseOrder purchaseOrder = resolvePurchaseOrder(line);
        String customerName = purchaseOrder != null && purchaseOrder.getCustomer() != null
                ? purchaseOrder.getCustomer().getCompanyName()
                : "";

        int assignedTotal = assignments.stream().mapToInt(WorkOrderStockAssignment::getQuantity).sum();
        List<StockAssignmentReportLine> reportLines = List.of(
                new StockAssignmentReportLine(stockAssignmentReportLocale.stockLocationLabel(), assignedTotal));

        Map<String, Object> params = new HashMap<>();
        params.put("reportTitle", stockAssignmentReportLocale.get("title"));
        params.put("labelWorkOrder", stockAssignmentReportLocale.get("workOrder"));
        params.put("labelPurchaseOrder", stockAssignmentReportLocale.get("purchaseOrder"));
        params.put("labelCustomer", stockAssignmentReportLocale.get("customer"));
        params.put("labelProduct", stockAssignmentReportLocale.get("product"));
        params.put("labelRequiredAssigned", stockAssignmentReportLocale.get("requiredAssigned"));
        params.put("labelAssignedAt", stockAssignmentReportLocale.get("assignedAt"));
        params.put("labelCreatedBy", stockAssignmentReportLocale.get("createdBy"));
        params.put("labelStockLocation", stockAssignmentReportLocale.get("stockLocation"));
        params.put("labelQuantity", stockAssignmentReportLocale.get("quantity"));
        params.put("workOrderId", workOrder.getId() != null ? "#" + workOrder.getId() : "—");
        params.put("purchaseOrderId", purchaseOrder != null && purchaseOrder.getId() > 0
                ? "#" + purchaseOrder.getId()
                : "—");
        params.put("customerName", customerName != null ? customerName : "");
        params.put("productReference", product != null && product.getReference() != null ? product.getReference() : "");
        params.put("productName", product != null && product.getName() != null ? product.getName() : "");
        params.put("requiredQuantity", line != null ? String.valueOf(line.getQuantity()) : "0");
        params.put("assignedTotal", String.valueOf(assignedTotal));
        params.put("assignedAt", assignments.get(0).getAssignedAt().format(stockAssignmentReportLocale.assignedAtFormatter()));
        params.put("createdBy", formatReportValue(createdByFullName));

        JasperPrint print = JasperFillManager.fillReport(getCompiledReport(), params, new JRBeanCollectionDataSource(reportLines));
        byte[] pdf = JasperExportManager.exportReportToPdf(print);
        return Base64.getEncoder().encodeToString(pdf);
    }

    private static String formatReportValue(String value) {
        return value != null && !value.isBlank() ? value.trim() : "—";
    }

    private JasperReport getCompiledReport() throws Exception {
        if (compiledReport == null) {
            synchronized (this) {
                if (compiledReport == null) {
                    ClassPathResource resource = new ClassPathResource("reports/stock-assignment-order.jrxml");
                    try (InputStream in = resource.getInputStream()) {
                        compiledReport = JasperCompileManager.compileReport(in);
                    }
                }
            }
        }
        return compiledReport;
    }

    private void creditProduct(Product product, int quantity) {
        StockedProduct stocked = stockedProductRepository.findByProduct_Id(product.getId())
                .orElseGet(() -> {
                    StockedProduct sp = new StockedProduct();
                    sp.setProduct(product);
                    sp.setQuantity(0);
                    return sp;
                });
        stocked.setQuantity(stocked.getQuantity() + quantity);
        stockedProductRepository.save(stocked);
    }

    private static int sumAllocationQuantities(List<WorkOrderStockAllocationTO> allocations) throws Exception {
        int total = 0;
        for (WorkOrderStockAllocationTO line : allocations) {
            if (line == null || line.getQuantity() == null || line.getQuantity() <= 0) {
                throw new Exception("WORK_ORDER_STOCK_ASSIGNMENT_QUANTITY_INVALID");
            }
            total += line.getQuantity();
        }
        return total;
    }

    private PurchaseOrder resolvePurchaseOrder(ProductOrder line) {
        if (line == null) {
            return null;
        }
        if (line.getPurchaseOrder() != null) {
            return line.getPurchaseOrder();
        }
        return productOrderRepository.findPurchaseOrderIdByProductOrderLineId(line.getId())
                .flatMap(purchaseOrderRepository::findById)
                .orElse(null);
    }
}
