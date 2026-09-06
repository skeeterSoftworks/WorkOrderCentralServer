package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Customer;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MachineBooking;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductMaterial;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.PurchaseOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockAssignmentOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Technology;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Tool;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MachineBookingRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.PurchaseOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.StockAssignmentOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.WorkOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.report.WorkOrderReportLocale;
import com.skeeterSoftworks.WorkOrderCentral.report.WorkOrderReportMaterialLine;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMachineBookingStatus;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkOrderReportService {

    private final WorkOrderRepository workOrderRepository;
    private final ProductOrderRepository productOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockAssignmentOrderRepository stockAssignmentOrderRepository;
    private final MachineBookingRepository machineBookingRepository;
    private final WorkOrderReportLocale reportLocale;
    private volatile JasperReport compiledReport;

    public WorkOrderReportService(
            WorkOrderRepository workOrderRepository,
            ProductOrderRepository productOrderRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            StockAssignmentOrderRepository stockAssignmentOrderRepository,
            MachineBookingRepository machineBookingRepository,
            WorkOrderReportLocale reportLocale) {
        this.workOrderRepository = workOrderRepository;
        this.productOrderRepository = productOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.stockAssignmentOrderRepository = stockAssignmentOrderRepository;
        this.machineBookingRepository = machineBookingRepository;
        this.reportLocale = reportLocale;
    }

    @Transactional(readOnly = true)
    public String generatePdfBase64ForWorkOrder(long workOrderId) throws Exception {
        if (workOrderId <= 0) {
            throw new Exception("INVALID_WORK_ORDER_ID");
        }
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new Exception("WORK_ORDER_NOT_FOUND"));
        return generatePdfBase64(workOrder);
    }

    private String generatePdfBase64(WorkOrder workOrder) throws Exception {
        ProductOrder line = workOrder.getProductOrder();
        if (line != null && line.getId() > 0 && line.getProduct() == null) {
            line = productOrderRepository.findById(line.getId()).orElse(line);
            workOrder.setProductOrder(line);
        }
        Product product = line != null ? line.getProduct() : null;
        if (product != null) {
            if (product.getProductMaterials() != null) {
                product.getProductMaterials().size();
            }
            if (product.getTechnologyData() != null) {
                Technology technology = product.getTechnologyData();
                if (technology.getTools() != null) {
                    technology.getTools().size();
                }
            }
        }

        PurchaseOrder purchaseOrder = resolvePurchaseOrder(line);
        Customer customer = purchaseOrder != null ? purchaseOrder.getCustomer() : null;
        int orderedQuantity = line != null && line.getQuantity() > 0 ? line.getQuantity() : 0;
        long stockAssigned = workOrder.getId() != null
                ? stockAssignmentOrderRepository.findByWorkOrder_IdOrderByIdAsc(workOrder.getId()).stream()
                .mapToInt(StockAssignmentOrder::getQuantity)
                .sum()
                : 0L;

        List<WorkOrderReportMaterialLine> materialLines = buildMaterialLines(product, orderedQuantity);
        List<MachineBooking> machineBookings = loadActiveMachineBookings(workOrder);
        boolean showMachineSection = !machineBookings.isEmpty();

        Map<String, Object> params = new HashMap<>();
        params.put("reportTitle", reportLocale.get("title"));
        params.put("labelWorkOrder", reportLocale.get("workOrder"));
        params.put("labelPurchaseOrder", reportLocale.get("purchaseOrder"));
        params.put("labelBuyerId", reportLocale.get("buyerId"));
        params.put("labelProduct", reportLocale.get("product"));
        params.put("labelProductQuantity", reportLocale.get("productQuantity"));
        params.put("labelStockAssignedQuantity", reportLocale.get("stockAssignedQuantity"));
        params.put("labelDueDate", reportLocale.get("dueDate"));
        params.put("labelStartDate", reportLocale.get("startDate"));
        params.put("labelEndDate", reportLocale.get("endDate"));
        params.put("labelComment", reportLocale.get("comment"));
        params.put("labelMaterialsTitle", reportLocale.get("materialsTitle"));
        params.put("labelMaterialCode", reportLocale.get("materialCode"));
        params.put("labelMaterialName", reportLocale.get("materialName"));
        params.put("labelQuantity", reportLocale.get("quantity"));
        params.put("noMaterials", reportLocale.get("noMaterials"));
        params.put("labelTechnologyTitle", reportLocale.get("technologyTitle"));
        params.put("labelMachineTitle", reportLocale.get("machineTitle"));
        params.put("workOrderId", workOrder.getId() != null ? "#" + workOrder.getId() : "—");
        params.put("purchaseOrderNumber", purchaseOrder != null && purchaseOrder.getId() > 0
                ? "#" + purchaseOrder.getId()
                : "—");
        params.put("buyerId", formatValue(customer != null ? customer.getBuyerId() : null));
        params.put("productLine", formatProductLine(product));
        params.put("productQuantity", String.valueOf(orderedQuantity));
        params.put("stockAssignedQuantity", stockAssigned > 0 ? String.valueOf(stockAssigned) : "—");
        params.put("dueDate", formatDate(workOrder.getDueDate()));
        params.put("startDate", formatDate(workOrder.getStartDate()));
        params.put("endDate", formatDate(workOrder.getEndDate()));
        params.put("comment", formatValue(workOrder.getComment()));
        params.put("hasMaterials", !materialLines.isEmpty());
        params.put("technologyText", formatTechnologyText(product != null ? product.getTechnologyData() : null));
        params.put("machineText", formatMachineText(machineBookings));
        params.put("showMachineSection", showMachineSection);

        JasperPrint print = JasperFillManager.fillReport(
                getCompiledReport(),
                params,
                new JRBeanCollectionDataSource(materialLines));
        byte[] pdf = JasperExportManager.exportReportToPdf(print);
        return Base64.getEncoder().encodeToString(pdf);
    }

    private List<WorkOrderReportMaterialLine> buildMaterialLines(Product product, int orderedQuantity) {
        if (product == null || product.getProductMaterials() == null || product.getProductMaterials().isEmpty()) {
            return List.of();
        }
        List<WorkOrderReportMaterialLine> lines = new ArrayList<>();
        for (ProductMaterial row : product.getProductMaterials()) {
            if (row.getMaterial() == null) {
                continue;
            }
            double perUnit = row.getQuantityPerProductUnit() > 0 ? row.getQuantityPerProductUnit() : 1d;
            double required = perUnit * orderedQuantity;
            lines.add(new WorkOrderReportMaterialLine(
                    formatValue(row.getMaterial().getCode()),
                    formatValue(row.getMaterial().getName()),
                    formatQuantity(required)));
        }
        return lines;
    }

    private List<MachineBooking> loadActiveMachineBookings(WorkOrder workOrder) {
        if (workOrder == null || workOrder.getId() == null) {
            return List.of();
        }
        return machineBookingRepository.findByWorkOrder(workOrder).stream()
                .filter(booking -> booking.getStatus() != EMachineBookingStatus.CANCELLED)
                .sorted(Comparator.comparing(
                        MachineBooking::getStartDateTime,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private String formatTechnologyText(Technology technology) {
        if (technology == null) {
            return reportLocale.get("noTechnology");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(reportLocale.get("cycleTime")).append(": ").append(formatValue(technology.getCycleTime())).append('\n');
        sb.append(reportLocale.get("norm100")).append(": ")
                .append(technology.getNorm100() != null ? technology.getNorm100() : "—").append('\n');
        sb.append(reportLocale.get("piecesPerMaterial")).append(": ")
                .append(technology.getPiecesPerMaterial() != null ? technology.getPiecesPerMaterial() : "—");
        List<Tool> tools = technology.getTools() != null ? technology.getTools() : List.of();
        if (!tools.isEmpty()) {
            sb.append('\n').append(reportLocale.get("tools")).append(':');
            tools.stream()
                    .sorted(Comparator.comparing(Tool::getOrderNumber, Comparator.nullsLast(Comparator.naturalOrder())))
                    .forEach(tool -> {
                        sb.append("\n- ");
                        if (tool.getOrderNumber() != null) {
                            sb.append('#').append(tool.getOrderNumber()).append(' ');
                        }
                        sb.append(formatValue(tool.getToolName()));
                        if (tool.getWorkingTime() != null) {
                            sb.append(" (").append(reportLocale.get("toolWorkingTime")).append(": ")
                                    .append(tool.getWorkingTime()).append(')');
                        }
                    });
        }
        return sb.toString();
    }

    private String formatMachineText(List<MachineBooking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return reportLocale.get("noMachine");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bookings.size(); i++) {
            MachineBooking booking = bookings.get(i);
            if (i > 0) {
                sb.append('\n');
            }
            String machineName = booking.getMachine() != null
                    ? formatValue(booking.getMachine().getMachineName())
                    : "—";
            sb.append(reportLocale.get("machineName")).append(": ").append(machineName).append('\n');
            sb.append(reportLocale.get("engagementFrom")).append(": ")
                    .append(booking.getStartDateTime() != null
                            ? booking.getStartDateTime().format(reportLocale.dateTimeFormatter())
                            : "—")
                    .append('\n');
            sb.append(reportLocale.get("engagementTo")).append(": ")
                    .append(booking.getEndDateTime() != null
                            ? booking.getEndDateTime().format(reportLocale.dateTimeFormatter())
                            : "—");
        }
        return sb.toString();
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

    private String formatProductLine(Product product) {
        if (product == null) {
            return "—";
        }
        String reference = product.getReference();
        String name = product.getName();
        if (StringUtils.hasText(reference) && StringUtils.hasText(name)) {
            return reference.trim() + " — " + name.trim();
        }
        if (StringUtils.hasText(reference)) {
            return reference.trim();
        }
        if (StringUtils.hasText(name)) {
            return name.trim();
        }
        return "—";
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(reportLocale.dateFormatter()) : "—";
    }

    private static String formatQuantity(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.000_001d) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format(java.util.Locale.ROOT, "%.3f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static String formatValue(String value) {
        return StringUtils.hasText(value) ? value.trim() : "—";
    }

    private JasperReport getCompiledReport() throws Exception {
        if (compiledReport == null) {
            synchronized (this) {
                if (compiledReport == null) {
                    ClassPathResource resource = new ClassPathResource("reports/work-order.jrxml");
                    try (InputStream in = resource.getInputStream()) {
                        compiledReport = JasperCompileManager.compileReport(in);
                    }
                }
            }
        }
        return compiledReport;
    }
}
