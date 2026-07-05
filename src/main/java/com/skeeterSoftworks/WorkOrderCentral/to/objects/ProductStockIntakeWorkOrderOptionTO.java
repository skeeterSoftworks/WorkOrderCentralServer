package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EWorkOrderState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStockIntakeWorkOrderOptionTO {
    private Long id;
    private String productReference;
    private String productName;
    private Integer requiredQuantity;
    private Long producedGoodQuantity;
    /** Total quantity already received to stock for this work order. */
    private Integer receivedToStockQuantity;
    private Boolean internalStockDemand;
    private EWorkOrderState state;
}
