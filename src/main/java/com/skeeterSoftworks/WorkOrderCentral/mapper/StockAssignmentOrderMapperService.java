package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockAssignmentOrder;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.StockAssignmentOrderTO;
import org.springframework.stereotype.Service;

@Service
public class StockAssignmentOrderMapperService {

    public StockAssignmentOrderTO mapToTO(StockAssignmentOrder order) {
        if (order == null) {
            return null;
        }
        StockAssignmentOrderTO to = new StockAssignmentOrderTO();
        to.setId(order.getId());
        to.setCode(order.getCode());
        if (order.getWorkOrder() != null) {
            to.setWorkOrderId(order.getWorkOrder().getId());
        }
        if (order.getProduct() != null) {
            to.setProductId(order.getProduct().getId());
            to.setProductReference(order.getProduct().getReference());
            to.setProductName(order.getProduct().getName());
        }
        to.setQuantity(order.getQuantity());
        to.setStatus(order.getStatus());
        to.setCreatedAt(order.getCreatedAt());
        to.setCreatedByFullName(order.getCreatedByFullName());
        to.setAssignedAt(order.getAssignedAt());
        to.setAssignedByUserQr(order.getAssignedByUserQr());
        return to;
    }
}
