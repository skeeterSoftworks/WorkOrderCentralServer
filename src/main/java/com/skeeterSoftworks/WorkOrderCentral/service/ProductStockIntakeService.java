package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductStockIntake;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.PurchaseOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductStockIntakeRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.WorkOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EProductStockIntakeUnitOfMeasure;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductStockIntakeTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductStockIntakeWorkOrderOptionTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ProductStockIntakeService {

    private static final int MAX_RECENT_LIMIT = 200;

    private final ProductStockIntakeRepository productStockIntakeRepository;
    private final ProductRepository productRepository;
    private final WorkOrderRepository workOrderRepository;
    private final StockProductInventoryService stockProductInventoryService;

    public ProductStockIntakeService(
            ProductStockIntakeRepository productStockIntakeRepository,
            ProductRepository productRepository,
            WorkOrderRepository workOrderRepository,
            StockProductInventoryService stockProductInventoryService) {
        this.productStockIntakeRepository = productStockIntakeRepository;
        this.productRepository = productRepository;
        this.workOrderRepository = workOrderRepository;
        this.stockProductInventoryService = stockProductInventoryService;
    }

    @Transactional(readOnly = true)
    public List<ProductStockIntakeTO> listRecent(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), MAX_RECENT_LIMIT);
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();
        return productStockIntakeRepository
                .findAllByReceivedAtGreaterThanEqualAndReceivedAtLessThanOrderByReceivedAtDescIdDesc(
                        dayStart, dayEnd, PageRequest.of(0, safeLimit))
                .stream()
                .map(this::toTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductStockIntakeWorkOrderOptionTO> listWorkOrderOptions(long productId) {
        if (productId <= 0) {
            return List.of();
        }
        return workOrderRepository.findByProductOrder_Product_IdOrderByIdDesc(productId).stream()
                .map(this::toWorkOrderOption)
                .sorted(Comparator.comparing(ProductStockIntakeWorkOrderOptionTO::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional
    public ProductStockIntakeTO recordIntake(ProductStockIntakeTO request) throws Exception {
        if (request == null || request.getProductId() == null || request.getProductId() <= 0) {
            throw new Exception("PRODUCT_STOCK_INTAKE_PRODUCT_REQUIRED");
        }
        if (request.getWorkOrderId() == null || request.getWorkOrderId() <= 0) {
            throw new Exception("PRODUCT_STOCK_INTAKE_WORK_ORDER_REQUIRED");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new Exception("PRODUCT_STOCK_INTAKE_INVALID_QUANTITY");
        }
        EProductStockIntakeUnitOfMeasure unit = request.getUnitOfMeasure() != null
                ? request.getUnitOfMeasure()
                : EProductStockIntakeUnitOfMeasure.PIECES;

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new Exception("PRODUCT_NOT_FOUND"));

        WorkOrder workOrder = workOrderRepository.findById(request.getWorkOrderId())
                .orElseThrow(() -> new Exception("PRODUCT_STOCK_INTAKE_WORK_ORDER_NOT_FOUND"));

        ProductOrder line = workOrder.getProductOrder();
        if (line == null || line.getProduct() == null || line.getProduct().getId() == null
                || !line.getProduct().getId().equals(product.getId())) {
            throw new Exception("PRODUCT_STOCK_INTAKE_PRODUCT_WORK_ORDER_MISMATCH");
        }

        int quantity = request.getQuantity();
        validateQuantityAgainstProduction(workOrder, quantity);
        int surplusQuantity = computeSurplusQuantity(workOrder, quantity);

        ProductStockIntake intake = new ProductStockIntake();
        intake.setProduct(product);
        intake.setWorkOrder(workOrder);
        intake.setStickerNumber(normalizeStickerNumber(request.getStickerNumber()));
        intake.setUnitOfMeasure(unit);
        intake.setQuantity(quantity);
        intake.setSurplusQuantity(surplusQuantity);
        intake.setReceivedAt(LocalDateTime.now());

        ProductStockIntake saved = productStockIntakeRepository.save(intake);
        stockProductInventoryService.creditProductStock(product, quantity);
        return toTO(saved);
    }

    private void validateQuantityAgainstProduction(WorkOrder workOrder, int quantity) throws Exception {
        long produced = Math.max(0, workOrder.getProducedGoodQuantity());
        long alreadyReceived = productStockIntakeRepository.sumQuantityByWorkOrderId(workOrder.getId());
        if ((long) quantity + alreadyReceived > produced) {
            throw new Exception("PRODUCT_STOCK_INTAKE_EXCEEDS_PRODUCED_QUANTITY");
        }
    }

    private int computeSurplusQuantity(WorkOrder workOrder, int quantity) {
        int required = workOrder.getProductOrder() != null ? workOrder.getProductOrder().getQuantity() : 0;
        long alreadyOrderFilled = productStockIntakeRepository.sumOrderQuantityByWorkOrderId(workOrder.getId());
        int remainingOrderNeed = Math.max(0, required - (int) Math.min(Integer.MAX_VALUE, alreadyOrderFilled));
        int orderPortion = Math.min(quantity, remainingOrderNeed);
        return quantity - orderPortion;
    }

    private ProductStockIntakeWorkOrderOptionTO toWorkOrderOption(WorkOrder workOrder) {
        ProductStockIntakeWorkOrderOptionTO option = new ProductStockIntakeWorkOrderOptionTO();
        option.setId(workOrder.getId());
        option.setProducedGoodQuantity(workOrder.getProducedGoodQuantity());
        option.setState(workOrder.getState());
        ProductOrder line = workOrder.getProductOrder();
        if (line != null) {
            option.setRequiredQuantity(line.getQuantity());
            if (line.getProduct() != null) {
                option.setProductReference(line.getProduct().getReference());
                option.setProductName(line.getProduct().getName());
            }
            PurchaseOrder purchaseOrder = line.getPurchaseOrder();
            if (purchaseOrder != null) {
                option.setInternalStockDemand(purchaseOrder.isInternalStockDemand());
            }
        }
        if (workOrder.getId() != null) {
            long received = productStockIntakeRepository.sumQuantityByWorkOrderId(workOrder.getId());
            long receivedForOrder = productStockIntakeRepository.sumOrderQuantityByWorkOrderId(workOrder.getId());
            option.setReceivedToStockQuantity((int) Math.min(Integer.MAX_VALUE, received));
            option.setReceivedOrderQuantity((int) Math.min(Integer.MAX_VALUE, receivedForOrder));
        }
        return option;
    }

    private static String normalizeStickerNumber(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private ProductStockIntakeTO toTO(ProductStockIntake intake) {
        ProductStockIntakeTO to = new ProductStockIntakeTO();
        to.setId(intake.getId());
        to.setQuantity(intake.getQuantity());
        to.setSurplusQuantity(intake.getSurplusQuantity());
        to.setStickerNumber(intake.getStickerNumber());
        to.setUnitOfMeasure(intake.getUnitOfMeasure());
        if (intake.getReceivedAt() != null) {
            to.setReceivedAt(intake.getReceivedAt().toString());
        }
        Product product = intake.getProduct();
        if (product != null) {
            to.setProductId(product.getId());
            to.setProductReference(product.getReference());
            to.setProductName(product.getName());
        }
        WorkOrder workOrder = intake.getWorkOrder();
        if (workOrder != null) {
            to.setWorkOrderId(workOrder.getId());
        }
        return to;
    }
}
