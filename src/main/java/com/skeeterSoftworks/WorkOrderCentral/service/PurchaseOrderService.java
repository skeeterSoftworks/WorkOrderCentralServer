package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.PurchaseOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.PurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }

    public Optional<PurchaseOrder> getPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id);
    }

    public PurchaseOrder savePurchaseOrder(PurchaseOrder purchaseOrder) {
        return purchaseOrderRepository.save(purchaseOrder);
    }

    public PurchaseOrder updatePurchaseOrder(PurchaseOrder purchaseOrder) throws Exception {
        if (purchaseOrder.getId() <= 0) {
            throw new IllegalArgumentException("INVALID_ID");
        }

        Optional<PurchaseOrder> existing = purchaseOrderRepository.findById(purchaseOrder.getId());
        if (existing.isEmpty()) {
            throw new Exception("PURCHASE_ORDER_NOT_FOUND");
        }
        return purchaseOrderRepository.save(purchaseOrder);
    }

    public void deletePurchaseOrder(Long id) throws Exception {
        Optional<PurchaseOrder> existing = purchaseOrderRepository.findById(id);
        if (existing.isEmpty()) {
            throw new Exception("PURCHASE_ORDER_NOT_FOUND");
        }
        purchaseOrderRepository.deleteById(id);
    }
}

