package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EProductStockIntakeUnitOfMeasure;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStockIntakeTO {
    private Long id;
    private Long productId;
    private String productReference;
    private String productName;
    private Long workOrderId;
    private Integer surplusQuantity;
    private String stickerNumber;
    private EProductStockIntakeUnitOfMeasure unitOfMeasure;
    private Integer quantity;
    /** ISO-8601 date-time. */
    private String receivedAt;
}
