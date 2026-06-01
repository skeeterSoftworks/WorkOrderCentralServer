package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialOrderReceptionTO {
    private Long id;
    private Long materialOrderId;
    private String materialOrderCode;
    private String materialCode;
    private String materialName;
    private String materialProviderName;
    private LocalDateTime receivedAt;
    private Integer receivedQuantity;
    private MaterialOrderReceptionInternalControlTO internalControl;
    /** Nominal diameter from linked material (0 = not defined). */
    private Float materialDiameter;
    /** Nominal weight from linked material (0 = not defined). */
    private Float materialWeight;
    /** Nominal length from linked material (0 = not defined). */
    private Float materialLength;
    /** Nominal width from linked material (0 = not defined). */
    private Float materialWidth;
    /** Quantities to add to stock locations; sum must equal {@link #receivedQuantity}. */
    private java.util.List<MaterialReceptionStockAllocationTO> stockAllocations;
}
