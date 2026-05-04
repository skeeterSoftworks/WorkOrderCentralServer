package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.PurchaseOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.WorkOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductAvailableStockTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockService {

    private final WorkOrderRepository workOrderRepository;

    @Autowired
    public StockService(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }

    /**
     * Per product: sum of (internal PO: all produced good) + (external PO: max(0, produced − line quantity)).
     */
    public List<ProductAvailableStockTO> getAvailableFinishedGoodStockByProduct() {
        Map<Long, Long> quantityByProductId = new HashMap<>();
        Map<Long, String> referenceByProductId = new HashMap<>();
        Map<Long, String> nameByProductId = new HashMap<>();

        for (WorkOrder wo : workOrderRepository.findAll()) {
            ProductOrder line = wo.getProductOrder();
            if (line == null) {
                continue;
            }
            Product product = line.getProduct();
            if (product == null || product.getId() == null) {
                continue;
            }
            long productId = product.getId();
            referenceByProductId.putIfAbsent(productId, product.getReference());
            nameByProductId.putIfAbsent(productId, product.getName());

            PurchaseOrder purchaseOrder = line.getPurchaseOrder();
            boolean internalDemand = purchaseOrder != null && purchaseOrder.isInternalStockDemand();

            long produced = wo.getProducedGoodQuantity();
            int required = line.getQuantity();

            long contribution;
            if (internalDemand) {
                contribution = produced;
            } else if (required > 0) {
                contribution = Math.max(0, produced - (long) required);
            } else {
                contribution = 0;
            }

            if (contribution > 0) {
                quantityByProductId.merge(productId, contribution, Long::sum);
            }
        }

        List<ProductAvailableStockTO> rows = new ArrayList<>();
        for (Map.Entry<Long, Long> e : quantityByProductId.entrySet()) {
            long pid = e.getKey();
            rows.add(new ProductAvailableStockTO(
                    pid,
                    referenceByProductId.get(pid),
                    nameByProductId.get(pid),
                    e.getValue()
            ));
        }

        rows.sort(Comparator.comparing(
                ProductAvailableStockTO::getProductReference,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        ));
        return rows;
    }
}
