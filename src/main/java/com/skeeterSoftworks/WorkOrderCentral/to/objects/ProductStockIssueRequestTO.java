package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStockIssueRequestTO {
    private Long workOrderId;
    /** When null or omitted, issues the full remaining quantity for the work order line. */
    private Integer quantity;
    private String operatorUserQrCode;
}
