package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.PurchaseOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockAssignmentOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockedProduct;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.PurchaseOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.StockAssignmentOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.StockedProductRepository;
import com.skeeterSoftworks.WorkOrderCentral.mapper.StockAssignmentOrderMapperService;
import com.skeeterSoftworks.WorkOrderCentral.report.StockAssignmentReportLine;
import com.skeeterSoftworks.WorkOrderCentral.report.StockAssignmentReportLocale;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EStockAssignmentOrderStatus;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductStockAvailabilityTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.StockAssignmentOrderTO;
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
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class StockProductInventoryService {

    private final StockedProductRepository stockedProductRepository;
    private final ProductRepository productRepository;
    private final StockAssignmentOrderRepository stockAssignmentOrderRepository;
    private final StockService stockService;
    private final ProductOrderRepository productOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockAssignmentReportLocale stockAssignmentReportLocale;
    private final StockAssignmentOrderMapperService stockAssignmentOrderMapperService;
    private final UsersService usersService;
    private volatile JasperReport compiledReport;

    public StockProductInventoryService(
            StockedProductRepository stockedProductRepository,
            ProductRepository productRepository,
            StockAssignmentOrderRepository stockAssignmentOrderRepository,
            StockService stockService,
            ProductOrderRepository productOrderRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            StockAssignmentReportLocale stockAssignmentReportLocale,
            StockAssignmentOrderMapperService stockAssignmentOrderMapperService,
            UsersService usersService) {
        this.stockedProductRepository = stockedProductRepository;
        this.productRepository = productRepository;
        this.stockAssignmentOrderRepository = stockAssignmentOrderRepository;
        this.stockService = stockService;
        this.productOrderRepository = productOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.stockAssignmentReportLocale = stockAssignmentReportLocale;
        this.stockAssignmentOrderMapperService = stockAssignmentOrderMapperService;
        this.usersService = usersService;
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

    @Transactional(readOnly = true)
    public StockAssignmentOrderTO getAssignmentOrderByCode(String code) throws Exception {
        StockAssignmentOrder order = findByNormalizedCode(code)
                .orElseThrow(() -> new Exception("STOCK_ASSIGNMENT_ORDER_NOT_FOUND"));
        return stockAssignmentOrderMapperService.mapToTO(order);
    }

    /**
     * Reserves quantity for a work order (status UNASSIGNED). Physical stock is reduced on stock-local fulfill.
     */
    @Transactional(readOnly = true)
    public long computeUnassignedAvailableQuantity(long productId) {
        long aggregateAvailable = stockService.getAvailableQuantityForProduct(productId);
        long reserved = stockAssignmentOrderRepository.sumReservedQuantityByProductId(productId);
        return Math.max(0, aggregateAvailable - reserved);
    }

    @Transactional
    public void ensureStockedProductsSyncedForProduct(long productId) {
        long aggregateAvailable = stockService.getAvailableQuantityForProduct(productId);
        long assignedReserved = stockAssignmentOrderRepository.sumQuantityByProductIdAndStatus(
                productId, EStockAssignmentOrderStatus.ASSIGNED);
        long targetPhysical = Math.max(0, aggregateAvailable - assignedReserved);
        long storedTotal = stockedProductRepository.sumQuantityByProductId(productId);
        if (targetPhysical <= storedTotal) {
            return;
        }
        int delta = (int) Math.min(Integer.MAX_VALUE, targetPhysical - storedTotal);
        if (delta <= 0) {
            return;
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalStateException("PRODUCT_NOT_FOUND"));
        creditProduct(product, delta);
    }

    @Transactional
    public List<StockAssignmentOrder> createStockAssignmentOrdersForWorkOrder(
            WorkOrder workOrder,
            List<WorkOrderStockAllocationTO> allocations,
            String createdByFullName) throws Exception {
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

        Product product = line.getProduct();
        StockAssignmentOrder order = new StockAssignmentOrder();
        order.setCode(generateUniqueEightDigitCode());
        order.setWorkOrder(workOrder);
        order.setProduct(product);
        order.setQuantity(assignedTotal);
        order.setStatus(EStockAssignmentOrderStatus.UNASSIGNED);
        order.setCreatedAt(LocalDateTime.now());
        order.setCreatedByFullName(StringUtils.hasText(createdByFullName) ? createdByFullName.trim() : null);
        return List.of(stockAssignmentOrderRepository.save(order));
    }

    @Transactional
    public StockAssignmentOrderTO fulfillAssignmentOrderByCode(String code, String operatorUserQrCode) throws Exception {
        StockAssignmentOrder order = findByNormalizedCode(code)
                .orElseThrow(() -> new Exception("STOCK_ASSIGNMENT_ORDER_NOT_FOUND"));
        if (order.getStatus() == EStockAssignmentOrderStatus.ASSIGNED) {
            throw new Exception("STOCK_ASSIGNMENT_ORDER_ALREADY_ASSIGNED");
        }
        long productId = order.getProduct().getId();
        ensureStockedProductsSyncedForProduct(productId);

        StockedProduct stocked = stockedProductRepository.findByProduct_Id(productId)
                .orElseThrow(() -> new Exception("STOCK_ASSIGNMENT_ORDER_INSUFFICIENT_STOCK"));
        if (stocked.getQuantity() < order.getQuantity()) {
            throw new Exception("STOCK_ASSIGNMENT_ORDER_INSUFFICIENT_STOCK");
        }
        stocked.setQuantity(stocked.getQuantity() - order.getQuantity());
        stockedProductRepository.save(stocked);

        order.setStatus(EStockAssignmentOrderStatus.ASSIGNED);
        order.setAssignedAt(LocalDateTime.now());
        if (StringUtils.hasText(operatorUserQrCode)) {
            String qr = operatorUserQrCode.trim();
            order.setAssignedByUserQr(qr);
            String fullName = usersService.resolveFullNameByQrCode(qr);
            if (StringUtils.hasText(fullName)) {
                order.setAssignedByFullName(fullName);
            }
        }
        return stockAssignmentOrderMapperService.mapToTO(stockAssignmentOrderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public String generateStockAssignmentOrderPdfBase64ForWorkOrder(long workOrderId) throws Exception {
        StockAssignmentOrder order = stockAssignmentOrderRepository.findFirstByWorkOrder_IdOrderByIdDesc(workOrderId)
                .orElseThrow(() -> new Exception("STOCK_ASSIGNMENT_ORDER_NOT_FOUND"));
        return generateStockAssignmentOrderPdfBase64(order);
    }

    public String generateStockAssignmentOrderPdfBase64(StockAssignmentOrder order) throws Exception {
        if (order == null) {
            return null;
        }
        WorkOrder workOrder = order.getWorkOrder();
        ProductOrder line = workOrder != null ? workOrder.getProductOrder() : null;
        Product product = order.getProduct() != null ? order.getProduct() : (line != null ? line.getProduct() : null);
        PurchaseOrder purchaseOrder = resolvePurchaseOrder(line);
        String customerName = purchaseOrder != null && purchaseOrder.getCustomer() != null
                ? purchaseOrder.getCustomer().getCompanyName()
                : "";

        List<StockAssignmentReportLine> reportLines = List.of(
                new StockAssignmentReportLine(stockAssignmentReportLocale.stockLocationLabel(), order.getQuantity()));

        LocalDateTime displayAssignedAt = order.getAssignedAt() != null ? order.getAssignedAt() : order.getCreatedAt();

        Map<String, Object> params = new HashMap<>();
        params.put("reportTitle", stockAssignmentReportLocale.get("title"));
        params.put("labelAssignmentOrderCode", stockAssignmentReportLocale.get("assignmentOrderCode"));
        params.put("labelWorkOrder", stockAssignmentReportLocale.get("workOrder"));
        params.put("labelPurchaseOrder", stockAssignmentReportLocale.get("purchaseOrder"));
        params.put("labelCustomer", stockAssignmentReportLocale.get("customer"));
        params.put("labelProduct", stockAssignmentReportLocale.get("product"));
        params.put("labelRequiredAssigned", stockAssignmentReportLocale.get("requiredAssigned"));
        params.put("labelAssignedAt", stockAssignmentReportLocale.get("assignedAt"));
        params.put("labelCreatedBy", stockAssignmentReportLocale.get("createdBy"));
        params.put("labelStockLocation", stockAssignmentReportLocale.get("stockLocation"));
        params.put("labelQuantity", stockAssignmentReportLocale.get("quantity"));
        params.put("assignmentOrderCode", order.getCode());
        params.put("workOrderId", workOrder != null && workOrder.getId() != null ? "#" + workOrder.getId() : "—");
        params.put("purchaseOrderId", purchaseOrder != null && purchaseOrder.getId() > 0
                ? "#" + purchaseOrder.getId()
                : "—");
        params.put("customerName", customerName != null ? customerName : "");
        params.put("productReference", product != null && product.getReference() != null ? product.getReference() : "");
        params.put("productName", product != null && product.getName() != null ? product.getName() : "");
        params.put("requiredQuantity", line != null ? String.valueOf(line.getQuantity()) : "0");
        params.put("assignedTotal", String.valueOf(order.getQuantity()));
        params.put("assignedAt", displayAssignedAt.format(stockAssignmentReportLocale.assignedAtFormatter()));
        params.put("createdBy", formatReportValue(order.getCreatedByFullName()));

        JasperPrint print = JasperFillManager.fillReport(getCompiledReport(), params, new JRBeanCollectionDataSource(reportLines));
        byte[] pdf = JasperExportManager.exportReportToPdf(print);
        return Base64.getEncoder().encodeToString(pdf);
    }

    private java.util.Optional<StockAssignmentOrder> findByNormalizedCode(String code) throws Exception {
        String normalized = normalizeAssignmentOrderCode(code);
        if (normalized == null) {
            throw new Exception("STOCK_ASSIGNMENT_ORDER_INVALID_CODE");
        }
        return stockAssignmentOrderRepository.findByCode(normalized);
    }

    static String normalizeAssignmentOrderCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        String digits = code.trim().replaceAll("\\s+", "");
        if (!digits.matches("\\d{8}")) {
            return null;
        }
        return digits;
    }

    private String generateUniqueEightDigitCode() {
        for (int attempt = 0; attempt < 200; attempt++) {
            int value = ThreadLocalRandom.current().nextInt(100_000_000);
            String code = String.format("%08d", value);
            if (!stockAssignmentOrderRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Could not generate unique stock assignment order code");
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
