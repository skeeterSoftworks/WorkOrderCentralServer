package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EStockOrderHistoryProductType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockOrderHistoryRowTO {
    private Long id;
    private String code;
    private EStockOrderHistoryProductType productType;
    private Long workOrderId;
    private Long productId;
    private String productReference;
    private String productName;
    private Integer quantity;
    private LocalDateTime assignedAt;
    private String assignedByFullName;
    private String assignedByUserQr;
}
