package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialReceptionStockAllocationTO {
    private Long stockLocationId;
    private Integer quantity;
    /** Optional visual marker for this allocation row (e.g. RED, BLUE). */
    private String colorMarker;
}
