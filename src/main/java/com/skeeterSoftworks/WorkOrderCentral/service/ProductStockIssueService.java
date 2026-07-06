package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductStockIssue;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.PurchaseOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockedProduct;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductStockIssueRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.PurchaseOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.StockAssignmentOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.StockedProductRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.WorkOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.report.ProductStockIssueReportLine;
import com.skeeterSoftworks.WorkOrderCentral.report.ProductStockIssueReportLocale;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EStockAssignmentOrderStatus;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EWorkOrderState;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductStockIssueRequestTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductStockIssueResultTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductStockIssueWorkOrderOptionTO;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductStockIssueService {

    private final WorkOrderRepository workOrderRepository;
    private final ProductStockIssueRepository productStockIssueRepository;
    private final StockAssignmentOrderRepository stockAssignmentOrderRepository;
    private final StockedProductRepository stockedProductRepository;
    private final ProductOrderRepository productOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockProductInventoryService stockProductInventoryService;
    private final PurchaseOrderService purchaseOrderService;
    private final UsersService usersService;
    private final ProductStockIssueReportLocale reportLocale;
    private volatile JasperReport compiledReport;

    public ProductStockIssueService(
            WorkOrderRepository workOrderRepository,
            ProductStockIssueRepository productStockIssueRepository,
            StockAssignmentOrderRepository stockAssignmentOrderRepository,
            StockedProductRepository stockedProductRepository,
            ProductOrderRepository productOrderRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            StockProductInventoryService stockProductInventoryService,
            PurchaseOrderService purchaseOrderService,
            UsersService usersService,
            ProductStockIssueReportLocale reportLocale) {
        this.workOrderRepository = workOrderRepository;
        this.productStockIssueRepository = productStockIssueRepository;
        this.stockAssignmentOrderRepository = stockAssignmentOrderRepository;
        this.stockedProductRepository = stockedProductRepository;
        this.productOrderRepository = productOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.stockProductInventoryService = stockProductInventoryService;
        this.purchaseOrderService = purchaseOrderService;
        this.usersService = usersService;
        this.reportLocale = reportLocale;
    }

    @Transactional(readOnly = true)
    public List<ProductStockIssueWorkOrderOptionTO> listEligibleWorkOrders() {
        return workOrderRepository.findByStateOrderByIdDesc(EWorkOrderState.COMPLETE).stream()
                .map(this::toWorkOrderOption)
                .filter(option -> option.getRemainingQuantity() != null && option.getRemainingQuantity() > 0)
                .sorted(Comparator.comparing(ProductStockIssueWorkOrderOptionTO::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional
    public ProductStockIssueResultTO issueFromStock(ProductStockIssueRequestTO request) throws Exception {
        if (request == null || request.getWorkOrderId() == null || request.getWorkOrderId() <= 0) {
            throw new Exception("PRODUCT_STOCK_ISSUE_WORK_ORDER_REQUIRED");
        }
        WorkOrder workOrder = workOrderRepository.findById(request.getWorkOrderId())
                .orElseThrow(() -> new Exception("PRODUCT_STOCK_ISSUE_WORK_ORDER_NOT_FOUND"));
        if (workOrder.getState() != EWorkOrderState.COMPLETE) {
            throw new Exception("PRODUCT_STOCK_ISSUE_WORK_ORDER_NOT_COMPLETE");
        }
        ProductOrder line = workOrder.getProductOrder();
        if (line == null || line.getProduct() == null || line.getProduct().getId() == null) {
            throw new Exception("PRODUCT_STOCK_ISSUE_PRODUCT_REQUIRED");
        }
        Product product = line.getProduct();
        int remaining = computeRemainingIssueQuantity(workOrder, line);
        if (remaining <= 0) {
            throw new Exception("PRODUCT_STOCK_ISSUE_ALREADY_FULFILLED");
        }
        int quantity = request.getQuantity() != null && request.getQuantity() > 0
                ? request.getQuantity()
                : remaining;
        if (quantity > remaining) {
            throw new Exception("PRODUCT_STOCK_ISSUE_EXCEEDS_REMAINING");
        }
        long available = stockProductInventoryService.computeUnassignedAvailableQuantity(product.getId());
        if (quantity > available) {
            throw new Exception("PRODUCT_STOCK_ISSUE_INSUFFICIENT_STOCK");
        }

        debitProductStock(product, quantity);

        String issuedByFullName = null;
        if (StringUtils.hasText(request.getOperatorUserQrCode())) {
            issuedByFullName = usersService.resolveFullNameByQrCode(request.getOperatorUserQrCode().trim());
        }
        LocalDateTime issuedAt = LocalDateTime.now();

        ProductStockIssue issue = new ProductStockIssue();
        issue.setWorkOrder(workOrder);
        issue.setProduct(product);
        issue.setQuantity(quantity);
        issue.setIssuedAt(issuedAt);
        if (StringUtils.hasText(request.getOperatorUserQrCode())) {
            issue.setIssuedByUserQr(request.getOperatorUserQrCode().trim());
        }
        issue.setIssuedByFullName(StringUtils.hasText(issuedByFullName) ? issuedByFullName.trim() : null);
        ProductStockIssue saved = productStockIssueRepository.save(issue);

        purchaseOrderService.onProductStockAssigned(workOrder.getId());

        ProductStockIssueResultTO result = new ProductStockIssueResultTO();
        result.setId(saved.getId());
        result.setWorkOrderId(workOrder.getId());
        result.setProductId(product.getId());
        result.setProductReference(product.getReference());
        result.setProductName(product.getName());
        result.setQuantity(quantity);
        result.setIssuedAt(issuedAt.toString());
        result.setIssuedByFullName(saved.getIssuedByFullName());
        result.setIssueReportPdfBase64(generateIssueReportPdfBase64(saved, workOrder, line, issuedByFullName));
        return result;
    }

    private ProductStockIssueWorkOrderOptionTO toWorkOrderOption(WorkOrder workOrder) {
        ProductStockIssueWorkOrderOptionTO option = new ProductStockIssueWorkOrderOptionTO();
        option.setId(workOrder.getId());
        ProductOrder line = workOrder.getProductOrder();
        if (line != null) {
            option.setRequiredQuantity(line.getQuantity());
            if (line.getProduct() != null) {
                option.setProductReference(line.getProduct().getReference());
                option.setProductName(line.getProduct().getName());
                option.setAvailableStockQuantity(
                        stockProductInventoryService.computeUnassignedAvailableQuantity(line.getProduct().getId()));
            }
            PurchaseOrder purchaseOrder = resolvePurchaseOrder(line);
            if (purchaseOrder != null) {
                option.setPurchaseOrderId(purchaseOrder.getId());
                if (purchaseOrder.getCustomer() != null) {
                    option.setCustomerName(purchaseOrder.getCustomer().getCompanyName());
                }
            }
        }
        if (workOrder.getId() != null && line != null) {
            int alreadyIssued = (int) Math.min(Integer.MAX_VALUE, totalOutboundQuantity(workOrder.getId()));
            option.setAlreadyIssuedQuantity(alreadyIssued);
            int required = line.getQuantity() > 0 ? line.getQuantity() : 0;
            option.setRemainingQuantity(Math.max(0, required - alreadyIssued));
        }
        return option;
    }

    private int computeRemainingIssueQuantity(WorkOrder workOrder, ProductOrder line) {
        if (workOrder.getId() == null) {
            return 0;
        }
        int required = line.getQuantity() > 0 ? line.getQuantity() : 0;
        long already = totalOutboundQuantity(workOrder.getId());
        return Math.max(0, required - (int) Math.min(Integer.MAX_VALUE, already));
    }

    private long totalOutboundQuantity(long workOrderId) {
        long fromAssignment = stockAssignmentOrderRepository.sumQuantityByWorkOrderIdAndStatus(
                workOrderId, EStockAssignmentOrderStatus.ASSIGNED);
        long fromIssue = productStockIssueRepository.sumQuantityByWorkOrderId(workOrderId);
        return fromAssignment + fromIssue;
    }

    private void debitProductStock(Product product, int quantity) throws Exception {
        StockedProduct stocked = stockedProductRepository.findByProduct_Id(product.getId())
                .orElseThrow(() -> new Exception("PRODUCT_STOCK_ISSUE_INSUFFICIENT_STOCK"));
        if (stocked.getQuantity() < quantity) {
            throw new Exception("PRODUCT_STOCK_ISSUE_INSUFFICIENT_STOCK");
        }
        stocked.setQuantity(stocked.getQuantity() - quantity);
        stockedProductRepository.save(stocked);
    }

    private String generateIssueReportPdfBase64(
            ProductStockIssue issue,
            WorkOrder workOrder,
            ProductOrder line,
            String issuedByFullName) throws Exception {
        PurchaseOrder purchaseOrder = resolvePurchaseOrder(line);
        Product product = issue.getProduct();
        ProductStockIssueReportLine reportLine = new ProductStockIssueReportLine(
                workOrder.getId() != null ? "#" + workOrder.getId() : "—",
                purchaseOrder != null && purchaseOrder.getId() > 0 ? "#" + purchaseOrder.getId() : "—",
                purchaseOrder != null && purchaseOrder.getCustomer() != null
                        ? formatReportValue(purchaseOrder.getCustomer().getCompanyName())
                        : "—",
                product != null ? formatReportValue(product.getReference()) : "—",
                product != null ? formatReportValue(product.getName()) : "—",
                issue.getQuantity());

        Map<String, Object> params = new HashMap<>();
        params.put("reportTitle", reportLocale.get("title"));
        params.put("labelIssuedAt", reportLocale.get("issuedAt"));
        params.put("labelIssuedBy", reportLocale.get("issuedBy"));
        params.put("labelWorkOrder", reportLocale.get("workOrder"));
        params.put("labelPurchaseOrder", reportLocale.get("purchaseOrder"));
        params.put("labelCustomer", reportLocale.get("customer"));
        params.put("labelProductReference", reportLocale.get("productReference"));
        params.put("labelProductName", reportLocale.get("productName"));
        params.put("labelQuantity", reportLocale.get("quantity"));
        params.put("issuedAt", reportLocale.issuedAtFormatter().format(issue.getIssuedAt()));
        params.put("issuedBy", formatReportValue(
                StringUtils.hasText(issuedByFullName) ? issuedByFullName : issue.getIssuedByFullName()));

        JasperPrint print = JasperFillManager.fillReport(
                getCompiledReport(),
                params,
                new JRBeanCollectionDataSource(List.of(reportLine)));
        byte[] pdf = JasperExportManager.exportReportToPdf(print);
        return Base64.getEncoder().encodeToString(pdf);
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

    private static String formatReportValue(String value) {
        return value != null && !value.isBlank() ? value.trim() : "—";
    }

    private JasperReport getCompiledReport() throws Exception {
        if (compiledReport == null) {
            synchronized (this) {
                if (compiledReport == null) {
                    ClassPathResource resource = new ClassPathResource("reports/product-stock-issue.jrxml");
                    try (InputStream in = resource.getInputStream()) {
                        compiledReport = JasperCompileManager.compileReport(in);
                    }
                }
            }
        }
        return compiledReport;
    }
}
