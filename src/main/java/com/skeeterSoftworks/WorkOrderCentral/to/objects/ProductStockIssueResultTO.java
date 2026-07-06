package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStockIssueResultTO {
    private Long id;
    private Long workOrderId;
    private Long productId;
    private String productReference;
    private String productName;
    private Integer quantity;
    private String issuedAt;
    private String issuedByFullName;
    private String issueReportPdfBase64;
}
