package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStockIssueWorkOrderOptionTO {
    private Long id;
    private String productReference;
    private String productName;
    private Long purchaseOrderId;
    private String customerName;
    private Integer requiredQuantity;
    private Integer alreadyIssuedQuantity;
    private Integer remainingQuantity;
    private Long availableStockQuantity;
}
