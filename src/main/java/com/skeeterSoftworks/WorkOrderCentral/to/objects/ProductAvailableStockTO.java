package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Finished-good stock derived from work orders: external surplus plus internal-demand production, combined per product.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAvailableStockTO {
    private Long productId;
    private String productReference;
    private String productName;
    private long availableQuantity;
}
