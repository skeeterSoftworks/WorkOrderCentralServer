package com.skeeterSoftworks.WorkOrderCentral.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStockIssueReportLine {
    private String workOrderId;
    private String purchaseOrderId;
    private String customerName;
    private String productReference;
    private String productName;
    private Integer quantity;
}
