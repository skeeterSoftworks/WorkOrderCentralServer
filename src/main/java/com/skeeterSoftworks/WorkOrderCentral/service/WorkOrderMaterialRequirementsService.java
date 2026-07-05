package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductMaterial;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.PurchaseOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.PurchaseOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.StockedMaterialRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.WorkOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.report.WorkOrderMaterialRequirementReportLine;
import com.skeeterSoftworks.WorkOrderCentral.report.WorkOrderMaterialRequirementsReportLocale;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EUnitOfMeasure;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.WorkOrderMaterialRequirementLineTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.WorkOrderMaterialRequirementsTO;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkOrderMaterialRequirementsService {

    private final ProductRepository productRepository;
    private final StockedMaterialRepository stockedMaterialRepository;
    private final WorkOrderRepository workOrderRepository;
    private final ProductOrderRepository productOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final WorkOrderMaterialRequirementsReportLocale reportLocale;
    private volatile JasperReport compiledReport;

    public WorkOrderMaterialRequirementsService(
            ProductRepository productRepository,
            StockedMaterialRepository stockedMaterialRepository,
            WorkOrderRepository workOrderRepository,
            ProductOrderRepository productOrderRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            WorkOrderMaterialRequirementsReportLocale reportLocale) {
        this.productRepository = productRepository;
        this.stockedMaterialRepository = stockedMaterialRepository;
        this.workOrderRepository = workOrderRepository;
        this.productOrderRepository = productOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.reportLocale = reportLocale;
    }

    @Transactional(readOnly = true)
    public WorkOrderMaterialRequirementsTO previewForProduct(long productId, int productQuantity) throws Exception {
        if (productId <= 0) {
            throw new Exception("INVALID_PRODUCT_ID");
        }
        if (productQuantity <= 0) {
            throw new Exception("INVALID_PRODUCT_QUANTITY");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new Exception("PRODUCT_NOT_FOUND"));
        if (product.getProductMaterials() != null) {
            product.getProductMaterials().size();
        }
        return buildRequirements(product, productQuantity);
    }

    @Transactional(readOnly = true)
    public WorkOrderMaterialRequirementsTO previewForWorkOrder(long workOrderId) throws Exception {
        if (workOrderId <= 0) {
            throw new Exception("INVALID_WORK_ORDER_ID");
        }
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new Exception("WORK_ORDER_NOT_FOUND"));
        ProductOrder line = workOrder.getProductOrder();
        if (line == null || line.getProduct() == null) {
            throw new Exception("WORK_ORDER_PRODUCT_REQUIRED");
        }
        Product product = line.getProduct();
        if (product.getProductMaterials() != null) {
            product.getProductMaterials().size();
        }
        int quantity = line.getQuantity() > 0 ? line.getQuantity() : 0;
        return buildRequirements(product, quantity);
    }

    @Transactional(readOnly = true)
    public String generatePdfBase64ForWorkOrder(long workOrderId, String createdByFullName) throws Exception {
        return generatePdfBase64ForWorkOrder(workOrderId, createdByFullName, null);
    }

    @Transactional(readOnly = true)
    public String generatePdfBase64ForWorkOrder(
            long workOrderId,
            String createdByFullName,
            String assignmentOrderCode) throws Exception {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new Exception("WORK_ORDER_NOT_FOUND"));
        WorkOrderMaterialRequirementsTO requirements = previewForWorkOrder(workOrderId);
        return generatePdfBase64(workOrder, requirements, createdByFullName, assignmentOrderCode);
    }

    public String generatePdfBase64ForPreview(
            WorkOrderMaterialRequirementsTO requirements,
            String createdByFullName) throws Exception {
        return generatePdfBase64(null, requirements, createdByFullName, null);
    }

    private WorkOrderMaterialRequirementsTO buildRequirements(Product product, int productQuantity) {
        WorkOrderMaterialRequirementsTO result = new WorkOrderMaterialRequirementsTO();
        result.setProductId(product.getId());
        result.setProductReference(product.getReference());
        result.setProductName(product.getName());
        result.setProductQuantity(productQuantity);

        List<ProductMaterial> bom = product.getProductMaterials() != null
                ? product.getProductMaterials()
                : List.of();
        result.setHasBillOfMaterials(!bom.isEmpty());

        List<WorkOrderMaterialRequirementLineTO> lines = new ArrayList<>();
        boolean fullyAvailable = true;
        for (ProductMaterial row : bom) {
            Material material = row.getMaterial();
            if (material == null || material.getId() == null) {
                continue;
            }
            double perUnit = row.getQuantityPerProductUnit() > 0 ? row.getQuantityPerProductUnit() : 1d;
            double required = perUnit * productQuantity;
            int available = (int) Math.min(Integer.MAX_VALUE, stockedMaterialRepository.sumQuantityByMaterialId(material.getId()));
            double missing = Math.max(0d, required - available);
            if (missing > 0.000_001d) {
                fullyAvailable = false;
            }
            EUnitOfMeasure unit = EUnitOfMeasure.PCS;
            lines.add(new WorkOrderMaterialRequirementLineTO(
                    material.getId(),
                    material.getCode(),
                    material.getName(),
                    unit,
                    required,
                    available,
                    missing));
        }
        result.setLines(lines);
        result.setFullyAvailable(fullyAvailable && !lines.isEmpty());
        if (lines.isEmpty()) {
            result.setFullyAvailable(false);
        }
        return result;
    }

    private String generatePdfBase64(
            WorkOrder workOrder,
            WorkOrderMaterialRequirementsTO requirements,
            String createdByFullName,
            String assignmentOrderCode) throws Exception {
        ProductOrder line = workOrder != null ? workOrder.getProductOrder() : null;
        PurchaseOrder purchaseOrder = resolvePurchaseOrder(line);
        String customerName = purchaseOrder != null && purchaseOrder.getCustomer() != null
                ? purchaseOrder.getCustomer().getCompanyName()
                : "";

        List<WorkOrderMaterialRequirementReportLine> reportLines = requirements.getLines().stream()
                .map(this::toReportLine)
                .toList();

        Map<String, Object> params = new HashMap<>();
        params.put("reportTitle", reportLocale.get("title"));
        params.put("labelWorkOrder", reportLocale.get("workOrder"));
        params.put("labelPurchaseOrder", reportLocale.get("purchaseOrder"));
        params.put("labelCustomer", reportLocale.get("customer"));
        params.put("labelProduct", reportLocale.get("product"));
        params.put("labelProductQuantity", reportLocale.get("productQuantity"));
        params.put("labelGeneratedAt", reportLocale.get("generatedAt"));
        params.put("labelCreatedBy", reportLocale.get("createdBy"));
        params.put("labelMaterialCode", reportLocale.get("materialCode"));
        params.put("labelMaterialName", reportLocale.get("materialName"));
        params.put("labelUnit", reportLocale.get("unit"));
        params.put("labelRequired", reportLocale.get("required"));
        params.put("labelAvailable", reportLocale.get("available"));
        params.put("labelMissing", reportLocale.get("missing"));
        params.put("labelAssignmentOrderCode", reportLocale.get("assignmentOrderCode"));
        params.put("noBillOfMaterials", reportLocale.get("noBillOfMaterials"));
        params.put("workOrderId", workOrder != null && workOrder.getId() != null ? "#" + workOrder.getId() : "—");
        params.put("purchaseOrderId", purchaseOrder != null && purchaseOrder.getId() > 0
                ? "#" + purchaseOrder.getId()
                : "—");
        params.put("customerName", customerName != null ? customerName : "");
        params.put("productReference", requirements.getProductReference() != null ? requirements.getProductReference() : "");
        params.put("productName", requirements.getProductName() != null ? requirements.getProductName() : "");
        params.put("productQuantity", String.valueOf(requirements.getProductQuantity()));
        params.put("generatedAt", LocalDateTime.now().format(reportLocale.generatedAtFormatter()));
        params.put("createdBy", formatReportValue(createdByFullName));
        params.put("assignmentOrderCode", StringUtils.hasText(assignmentOrderCode) ? assignmentOrderCode.trim() : "—");

        JasperPrint print = JasperFillManager.fillReport(
                getCompiledReport(),
                params,
                new JRBeanCollectionDataSource(reportLines));
        byte[] pdf = JasperExportManager.exportReportToPdf(print);
        return Base64.getEncoder().encodeToString(pdf);
    }

    private WorkOrderMaterialRequirementReportLine toReportLine(WorkOrderMaterialRequirementLineTO line) {
        return new WorkOrderMaterialRequirementReportLine(
                line.getMaterialCode() != null ? line.getMaterialCode() : "",
                line.getMaterialName() != null ? line.getMaterialName() : "",
                formatUnit(line.getUnitOfMeasure()),
                formatQuantity(line.getRequiredQuantity()),
                String.valueOf(line.getAvailableQuantity()),
                formatQuantity(line.getMissingQuantity()));
    }

    private String formatUnit(EUnitOfMeasure unit) {
        if (unit == null) {
            return EUnitOfMeasure.PCS.name();
        }
        return unit.name();
    }

    private static String formatQuantity(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.000_001d) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format(java.util.Locale.ROOT, "%.3f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static String formatReportValue(String value) {
        return value != null && !value.isBlank() ? value.trim() : "—";
    }

    private JasperReport getCompiledReport() throws Exception {
        if (compiledReport == null) {
            synchronized (this) {
                if (compiledReport == null) {
                    ClassPathResource resource = new ClassPathResource("reports/work-order-material-requirements.jrxml");
                    try (InputStream in = resource.getInputStream()) {
                        compiledReport = JasperCompileManager.compileReport(in);
                    }
                }
            }
        }
        return compiledReport;
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
