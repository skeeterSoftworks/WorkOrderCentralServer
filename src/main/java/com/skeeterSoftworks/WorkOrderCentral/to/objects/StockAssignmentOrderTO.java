package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EStockAssignmentOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockAssignmentOrderTO {
    private Long id;
    private String code;
    private Long workOrderId;
    private Long productId;
    private String productReference;
    private String productName;
    private Integer quantity;
    private EStockAssignmentOrderStatus status;
    private LocalDateTime createdAt;
    private String createdByFullName;
    private LocalDateTime assignedAt;
    private String assignedByUserQr;
}
