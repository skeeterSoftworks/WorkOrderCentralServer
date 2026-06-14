package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockAssignmentOrder;
import com.skeeterSoftworks.WorkOrderCentral.service.UsersService;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EStockOrderHistoryProductType;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.StockOrderHistoryRowTO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StockOrderHistoryMapperService {

    private final UsersService usersService;

    public StockOrderHistoryMapperService(UsersService usersService) {
        this.usersService = usersService;
    }

    public StockOrderHistoryRowTO mapFinishedProductAssignment(StockAssignmentOrder order) {
        if (order == null) {
            return null;
        }
        StockOrderHistoryRowTO row = new StockOrderHistoryRowTO();
        row.setId(order.getId());
        row.setCode(order.getCode());
        row.setProductType(EStockOrderHistoryProductType.FINISHED_PRODUCT);
        if (order.getWorkOrder() != null) {
            row.setWorkOrderId(order.getWorkOrder().getId());
        }
        if (order.getProduct() != null) {
            row.setProductId(order.getProduct().getId());
            row.setProductReference(order.getProduct().getReference());
            row.setProductName(order.getProduct().getName());
        }
        row.setQuantity(order.getQuantity());
        row.setAssignedAt(order.getAssignedAt());
        row.setAssignedByUserQr(order.getAssignedByUserQr());
        row.setAssignedByFullName(resolveAssignedByFullName(order));
        return row;
    }

    private String resolveAssignedByFullName(StockAssignmentOrder order) {
        if (StringUtils.hasText(order.getAssignedByFullName())) {
            return order.getAssignedByFullName().trim();
        }
        return usersService.resolveFullNameByQrCode(order.getAssignedByUserQr());
    }
}
