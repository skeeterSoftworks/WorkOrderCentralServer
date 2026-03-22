package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.PurchaseOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.PurchaseOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.WorkOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EPurchaseOrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final WorkOrderRepository workOrderRepository;

    @Autowired
    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository, WorkOrderRepository workOrderRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.workOrderRepository = workOrderRepository;
    }

    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }

    public Optional<PurchaseOrder> getPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id);
    }

    public PurchaseOrder savePurchaseOrder(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getId() == 0 && purchaseOrder.getCreatedAt() == null) {
            purchaseOrder.setCreatedAt(LocalDateTime.now());
        }
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
        if (workOrderRepository.existsByProductOrder_PurchaseOrder_Id(id)) {
            throw new Exception("PURCHASE_ORDER_HAS_WORK_ORDER");
        }
        purchaseOrderRepository.deleteById(id);
    }

    public void markConfirmed(Long purchaseOrderId) {
        purchaseOrderRepository.findById(purchaseOrderId).ifPresent(po -> {
            if (po.getConfirmedAt() == null) {
                po.setConfirmedAt(LocalDateTime.now());
                po.setOrderStatus(EPurchaseOrderStatus.CONFIRMED);
                purchaseOrderRepository.save(po);
            }
        });
    }
}

