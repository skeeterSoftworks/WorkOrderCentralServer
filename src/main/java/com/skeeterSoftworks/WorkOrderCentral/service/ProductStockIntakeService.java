package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductStockIntake;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductStockIntakeRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EProductStockIntakeUnitOfMeasure;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductStockIntakeTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductStockIntakeService {

    private static final int DEFAULT_RECENT_LIMIT = 50;
    private static final int MAX_RECENT_LIMIT = 200;

    private final ProductStockIntakeRepository productStockIntakeRepository;
    private final ProductRepository productRepository;
    private final StockProductInventoryService stockProductInventoryService;

    public ProductStockIntakeService(
            ProductStockIntakeRepository productStockIntakeRepository,
            ProductRepository productRepository,
            StockProductInventoryService stockProductInventoryService) {
        this.productStockIntakeRepository = productStockIntakeRepository;
        this.productRepository = productRepository;
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

    @Transactional
    public ProductStockIntakeTO recordIntake(ProductStockIntakeTO request) throws Exception {
        if (request == null || request.getProductId() == null || request.getProductId() <= 0) {
            throw new Exception("PRODUCT_STOCK_INTAKE_PRODUCT_REQUIRED");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new Exception("PRODUCT_STOCK_INTAKE_INVALID_QUANTITY");
        }
        EProductStockIntakeUnitOfMeasure unit = request.getUnitOfMeasure() != null
                ? request.getUnitOfMeasure()
                : EProductStockIntakeUnitOfMeasure.PIECES;

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new Exception("PRODUCT_NOT_FOUND"));

        ProductStockIntake intake = new ProductStockIntake();
        intake.setProduct(product);
        intake.setStickerNumber(normalizeStickerNumber(request.getStickerNumber()));
        intake.setUnitOfMeasure(unit);
        intake.setQuantity(request.getQuantity());
        intake.setReceivedAt(LocalDateTime.now());

        ProductStockIntake saved = productStockIntakeRepository.save(intake);
        stockProductInventoryService.creditProductStock(product, request.getQuantity());
        return toTO(saved);
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
        return to;
    }
}
