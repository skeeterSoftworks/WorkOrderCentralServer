package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStockAvailabilityTO {
    private Long productId;
    private String productReference;
    private String productName;
    private long availableQuantity;
}
