package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.PurchaseOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.PurchaseOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.WorkOrderTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkOrderMapperService {

    private final PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    public WorkOrderMapperService(PurchaseOrderRepository purchaseOrderRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    public WorkOrderTO mapToTO(WorkOrder workOrder) {
        if (workOrder == null) return null;
        WorkOrderTO to = new WorkOrderTO();
        to.setId(workOrder.getId());
        if (workOrder.getPurchaseOrder() != null) {
            to.setPurchaseOrderId(workOrder.getPurchaseOrder().getId());
        }
        to.setDueDate(workOrder.getDueDate());
        to.setStartDate(workOrder.getStartDate());
        to.setEndDate(workOrder.getEndDate());
        to.setComment(workOrder.getComment());
        return to;
    }

    public WorkOrder mapToEntity(WorkOrderTO to) {
        if (to == null) return null;
        WorkOrder workOrder = new WorkOrder();
        if (to.getId() != null) {
            workOrder.setId(to.getId());
        }
        if (to.getPurchaseOrderId() != null) {
            purchaseOrderRepository.findById(to.getPurchaseOrderId()).ifPresent(workOrder::setPurchaseOrder);
        }
        workOrder.setDueDate(to.getDueDate());
        workOrder.setStartDate(to.getStartDate());
        workOrder.setEndDate(to.getEndDate());
        workOrder.setComment(to.getComment());
        return workOrder;
    }
}
