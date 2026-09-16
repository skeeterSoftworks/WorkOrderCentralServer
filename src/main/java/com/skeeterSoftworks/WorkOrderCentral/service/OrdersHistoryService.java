package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Customer;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.DeliveryNote;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderLine;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductStockIntake;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductStockIssue;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.PurchaseOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockAssignmentOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.DeliveryNoteRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialOrderLineRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductStockIntakeRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductStockIssueRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.StockAssignmentOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EOrdersHistoryEventType;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EStockAssignmentOrderStatus;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderHistoryPageTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderHistoryRowTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductOrderHistoryPageTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductOrderHistoryRowTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class OrdersHistoryService {

    private static final int DEFAULT_SIZE = 25;
    private static final int MAX_SIZE = 100;

    private final ProductOrderRepository productOrderRepository;
    private final MaterialOrderLineRepository materialOrderLineRepository;
    private final ProductStockIntakeRepository productStockIntakeRepository;
    private final ProductStockIssueRepository productStockIssueRepository;
    private final StockAssignmentOrderRepository stockAssignmentOrderRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;

    public OrdersHistoryService(
            ProductOrderRepository productOrderRepository,
            MaterialOrderLineRepository materialOrderLineRepository,
            ProductStockIntakeRepository productStockIntakeRepository,
            ProductStockIssueRepository productStockIssueRepository,
            StockAssignmentOrderRepository stockAssignmentOrderRepository,
            DeliveryNoteRepository deliveryNoteRepository) {
        this.productOrderRepository = productOrderRepository;
        this.materialOrderLineRepository = materialOrderLineRepository;
        this.productStockIntakeRepository = productStockIntakeRepository;
        this.productStockIssueRepository = productStockIssueRepository;
        this.stockAssignmentOrderRepository = stockAssignmentOrderRepository;
        this.deliveryNoteRepository = deliveryNoteRepository;
    }

    @Transactional(readOnly = true)
    public ProductOrderHistoryPageTO searchProductOrders(String productReference, Long customerId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        List<ProductOrder> lines = productOrderRepository.findAll(
                ProductOrderHistorySpecifications.from(productReference, customerId));
        List<ProductOrderHistoryRowTO> events = new ArrayList<>(lines.size() * 2);
        for (ProductOrder line : lines) {
            events.add(mapProductOrderCreated(line));
        }

        List<Long> productOrderIds = lines.stream()
                .map(ProductOrder::getId)
                .filter(id -> id > 0)
                .distinct()
                .toList();
        if (!productOrderIds.isEmpty()) {
            for (ProductStockIntake intake : productStockIntakeRepository.findByProductOrderIds(productOrderIds)) {
                events.add(mapProductStockIn(intake));
            }
            for (ProductStockIssue issue : productStockIssueRepository.findByProductOrderIds(productOrderIds)) {
                events.add(mapProductStockOutIssue(issue));
            }
            for (StockAssignmentOrder assignment : stockAssignmentOrderRepository.findAssignedByProductOrderIds(
                    productOrderIds, EStockAssignmentOrderStatus.ASSIGNED)) {
                events.add(mapProductStockOutAssignment(assignment));
            }
        }

        return pageProductEvents(events, safePage, safeSize);
    }

    @Transactional(readOnly = true)
    public MaterialOrderHistoryPageTO searchMaterialOrders(
            String materialCode, Long materialProviderId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        List<MaterialOrderLine> lines = materialOrderLineRepository.findAll(
                MaterialOrderHistorySpecifications.from(materialCode, materialProviderId));
        List<MaterialOrderHistoryRowTO> events = new ArrayList<>(lines.size() * 2);
        for (MaterialOrderLine line : lines) {
            events.add(mapMaterialOrderCreated(line));
        }

        List<Long> lineIds = lines.stream()
                .map(MaterialOrderLine::getId)
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .distinct()
                .toList();
        if (!lineIds.isEmpty()) {
            for (DeliveryNote note : deliveryNoteRepository.findByMaterialOrderLine_IdIn(lineIds)) {
                events.add(mapMaterialStockIn(note));
            }
        }

        return pageMaterialEvents(events, safePage, safeSize);
    }

    private ProductOrderHistoryPageTO pageProductEvents(
            List<ProductOrderHistoryRowTO> events, int safePage, int safeSize) {
        events.sort(Comparator
                .comparing(ProductOrderHistoryRowTO::getEventAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ProductOrderHistoryRowTO::getRowKey, Comparator.nullsLast(Comparator.reverseOrder())));
        long total = events.size();
        int from = Math.min(safePage * safeSize, events.size());
        int to = Math.min(from + safeSize, events.size());
        return new ProductOrderHistoryPageTO(events.subList(from, to), total, safePage, safeSize);
    }

    private MaterialOrderHistoryPageTO pageMaterialEvents(
            List<MaterialOrderHistoryRowTO> events, int safePage, int safeSize) {
        events.sort(Comparator
                .comparing(MaterialOrderHistoryRowTO::getEventAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(MaterialOrderHistoryRowTO::getRowKey, Comparator.nullsLast(Comparator.reverseOrder())));
        long total = events.size();
        int from = Math.min(safePage * safeSize, events.size());
        int to = Math.min(from + safeSize, events.size());
        return new MaterialOrderHistoryPageTO(events.subList(from, to), total, safePage, safeSize);
    }

    private ProductOrderHistoryRowTO mapProductOrderCreated(ProductOrder line) {
        ProductOrderHistoryRowTO to = new ProductOrderHistoryRowTO();
        to.setId(line.getId());
        to.setRowKey("ORDER-" + line.getId());
        to.setEventType(EOrdersHistoryEventType.ORDER_CREATED);
        to.setQuantity(line.getQuantity());
        to.setPricePerUnit(line.getPricePerUnit());
        PurchaseOrder po = line.getPurchaseOrder();
        if (po != null) {
            LocalDateTime at = po.getCreatedAt();
            to.setEventAt(at);
            to.setOrderedAt(at);
            to.setPurchaseOrderId(po.getId());
            to.setPurchaseOrderCode(po.getCode());
            to.setOrderStatus(po.getOrderStatus());
            to.setCurrency(po.getCurrency());
            Customer customer = po.getCustomer();
            if (customer != null) {
                to.setCustomerId(customer.getId());
                to.setCustomerName(customer.getCompanyName());
                to.setBuyerId(customer.getBuyerId());
            }
        }
        Product product = line.getProduct();
        if (product != null) {
            to.setProductId(product.getId());
            to.setProductReference(product.getReference());
            to.setProductName(product.getName());
        }
        return to;
    }

    private ProductOrderHistoryRowTO mapProductStockIn(ProductStockIntake intake) {
        ProductOrderHistoryRowTO to = baseProductStockRow(intake.getId(), intake.getWorkOrder(), intake.getProduct());
        to.setRowKey("STOCK_IN-" + intake.getId());
        to.setEventType(EOrdersHistoryEventType.STOCK_IN);
        to.setEventAt(intake.getReceivedAt());
        to.setOrderedAt(intake.getReceivedAt());
        to.setQuantity(intake.getQuantity());
        return to;
    }

    private ProductOrderHistoryRowTO mapProductStockOutIssue(ProductStockIssue issue) {
        ProductOrderHistoryRowTO to = baseProductStockRow(issue.getId(), issue.getWorkOrder(), issue.getProduct());
        to.setRowKey("STOCK_OUT-PSI-" + issue.getId());
        to.setEventType(EOrdersHistoryEventType.STOCK_OUT);
        to.setEventAt(issue.getIssuedAt());
        to.setOrderedAt(issue.getIssuedAt());
        to.setQuantity(issue.getQuantity());
        to.setActorFullName(blankToNull(issue.getIssuedByFullName()));
        return to;
    }

    private ProductOrderHistoryRowTO mapProductStockOutAssignment(StockAssignmentOrder assignment) {
        ProductOrderHistoryRowTO to = baseProductStockRow(
                assignment.getId(), assignment.getWorkOrder(), assignment.getProduct());
        to.setRowKey("STOCK_OUT-SAO-" + assignment.getId());
        to.setEventType(EOrdersHistoryEventType.STOCK_OUT);
        to.setEventAt(assignment.getAssignedAt());
        to.setOrderedAt(assignment.getAssignedAt());
        to.setQuantity(assignment.getQuantity());
        to.setActorFullName(blankToNull(assignment.getAssignedByFullName()));
        return to;
    }

    private ProductOrderHistoryRowTO baseProductStockRow(Long id, WorkOrder workOrder, Product product) {
        ProductOrderHistoryRowTO to = new ProductOrderHistoryRowTO();
        to.setId(id);
        if (workOrder != null) {
            to.setWorkOrderId(workOrder.getId());
            to.setWorkOrderCode(workOrder.getCode());
            ProductOrder line = workOrder.getProductOrder();
            if (line != null) {
                PurchaseOrder po = line.getPurchaseOrder();
                if (po != null) {
                    to.setPurchaseOrderId(po.getId());
                    to.setPurchaseOrderCode(po.getCode());
                    to.setOrderStatus(po.getOrderStatus());
                    to.setCurrency(po.getCurrency());
                    Customer customer = po.getCustomer();
                    if (customer != null) {
                        to.setCustomerId(customer.getId());
                        to.setCustomerName(customer.getCompanyName());
                        to.setBuyerId(customer.getBuyerId());
                    }
                }
            }
        }
        if (product != null) {
            to.setProductId(product.getId());
            to.setProductReference(product.getReference());
            to.setProductName(product.getName());
        }
        return to;
    }

    private MaterialOrderHistoryRowTO mapMaterialOrderCreated(MaterialOrderLine line) {
        MaterialOrderHistoryRowTO to = new MaterialOrderHistoryRowTO();
        to.setId(line.getId());
        to.setRowKey("ORDER-" + line.getId());
        to.setEventType(EOrdersHistoryEventType.ORDER_CREATED);
        to.setQuantity(line.getQuantity());
        to.setPricePerUnit(line.getPricePerUnit());
        to.setUnitOfMeasure(line.getUnitOfMeasure());
        MaterialOrder order = line.getMaterialOrder();
        if (order != null) {
            LocalDateTime at = order.getCreatedAt();
            to.setEventAt(at);
            to.setOrderedAt(at);
            to.setMaterialOrderId(order.getId());
            to.setMaterialOrderCode(order.getCode());
            to.setStatus(order.getStatus());
            MaterialProvider provider = order.getMaterialProvider();
            if (provider != null) {
                to.setMaterialProviderId(provider.getId());
                to.setMaterialProviderName(provider.getName());
            }
        }
        Material material = line.getMaterial();
        if (material != null) {
            to.setMaterialId(material.getId());
            to.setMaterialCode(material.getCode());
            to.setMaterialName(material.getName());
        }
        return to;
    }

    private MaterialOrderHistoryRowTO mapMaterialStockIn(DeliveryNote note) {
        MaterialOrderHistoryRowTO to = new MaterialOrderHistoryRowTO();
        to.setId(note.getId());
        to.setRowKey("STOCK_IN-" + note.getId());
        to.setEventType(EOrdersHistoryEventType.STOCK_IN);
        to.setEventAt(note.getReceivedAt());
        to.setOrderedAt(note.getReceivedAt());
        to.setQuantity(note.getQuantity());
        to.setDeliveryNoteNumber(note.getDeliveryNoteNumber());
        MaterialOrder order = note.getMaterialOrder();
        if (order != null) {
            to.setMaterialOrderId(order.getId());
            to.setMaterialOrderCode(order.getCode());
            to.setStatus(order.getStatus());
            MaterialProvider provider = order.getMaterialProvider();
            if (provider != null) {
                to.setMaterialProviderId(provider.getId());
                to.setMaterialProviderName(provider.getName());
            }
        }
        MaterialOrderLine line = note.getMaterialOrderLine();
        if (line != null) {
            to.setUnitOfMeasure(line.getUnitOfMeasure());
            to.setPricePerUnit(line.getPricePerUnit());
            Material material = line.getMaterial();
            if (material != null) {
                to.setMaterialId(material.getId());
                to.setMaterialCode(material.getCode());
                to.setMaterialName(material.getName());
            }
        }
        return to;
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
