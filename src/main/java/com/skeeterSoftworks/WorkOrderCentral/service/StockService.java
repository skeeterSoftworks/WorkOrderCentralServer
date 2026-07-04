package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockedProduct;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.StockAssignmentOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.StockedProductRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductAvailableStockTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class StockService {

    private final StockedProductRepository stockedProductRepository;
    private final StockAssignmentOrderRepository stockAssignmentOrderRepository;

    public StockService(
            StockedProductRepository stockedProductRepository,
            StockAssignmentOrderRepository stockAssignmentOrderRepository) {
        this.stockedProductRepository = stockedProductRepository;
        this.stockAssignmentOrderRepository = stockAssignmentOrderRepository;
    }

    /**
     * Physical finished-goods stock minus unassigned assignment reservations.
     */
    public List<ProductAvailableStockTO> getAvailableFinishedGoodStockByProduct() {
        List<ProductAvailableStockTO> rows = new ArrayList<>();
        for (StockedProduct stocked : stockedProductRepository.findAll()) {
            Product product = stocked.getProduct();
            if (product == null || product.getId() == null) {
                continue;
            }
            long available = computeUnassignedPhysicalQuantity(product.getId(), stocked.getQuantity());
            if (available <= 0) {
                continue;
            }
            rows.add(new ProductAvailableStockTO(
                    product.getId(),
                    product.getReference(),
                    product.getName(),
                    available));
        }
        rows.sort(Comparator.comparing(
                ProductAvailableStockTO::getProductReference,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        return rows;
    }

    /**
     * Finished-good quantity available for assignment for a single product (physical stock minus reservations).
     */
    public long getAvailableQuantityForProduct(long productId) {
        if (productId <= 0) {
            return 0;
        }
        int physical = stockedProductRepository.findByProduct_Id(productId)
                .map(StockedProduct::getQuantity)
                .orElse(0);
        return computeUnassignedPhysicalQuantity(productId, physical);
    }

    private long computeUnassignedPhysicalQuantity(long productId, int physicalQuantity) {
        long reserved = stockAssignmentOrderRepository.sumReservedQuantityByProductId(productId);
        return Math.max(0, (long) physicalQuantity - reserved);
    }
}
