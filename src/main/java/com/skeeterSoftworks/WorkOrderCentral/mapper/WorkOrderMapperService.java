package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.WorkOrderTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkOrderMapperService {

    private final ProductOrderRepository productOrderRepository;

    @Autowired
    public WorkOrderMapperService(ProductOrderRepository productOrderRepository) {
        this.productOrderRepository = productOrderRepository;
    }

    public WorkOrderTO mapToTO(WorkOrder workOrder) {
        if (workOrder == null) {
            return null;
        }
        WorkOrderTO to = new WorkOrderTO();
        to.setId(workOrder.getId());
        ProductOrder line = workOrder.getProductOrder();
        if (line != null) {
            to.setProductOrderId(line.getId());
            to.setRequiredQuantity(line.getQuantity());
            if (line.getPurchaseOrder() != null) {
                to.setPurchaseOrderId(line.getPurchaseOrder().getId());
            }
            if (line.getProduct() != null) {
                to.setProductName(line.getProduct().getName());
                to.setProductReference(line.getProduct().getReference());
            }
        }
        to.setDueDate(workOrder.getDueDate());
        to.setStartDate(workOrder.getStartDate());
        to.setEndDate(workOrder.getEndDate());
        to.setComment(workOrder.getComment());
        to.setProducedGoodQuantity(workOrder.getProducedGoodQuantity());
        return to;
    }

    public WorkOrder mapToEntity(WorkOrderTO to) {
        if (to == null) {
            return null;
        }
        WorkOrder workOrder = new WorkOrder();
        if (to.getId() != null) {
            workOrder.setId(to.getId());
        }
        if (to.getProductOrderId() != null) {
            productOrderRepository.findById(to.getProductOrderId()).ifPresent(workOrder::setProductOrder);
        }
        workOrder.setDueDate(to.getDueDate());
        workOrder.setStartDate(to.getStartDate());
        workOrder.setEndDate(to.getEndDate());
        workOrder.setComment(to.getComment());
        return workOrder;
    }
}
