package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EUnitOfMeasure;
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
    private Long materialOrderLineId;
    private String materialOrderCode;
    private String materialCode;
    private String materialName;
    private String materialProviderName;
    /** ISO-8601 date-time of physical reception. */
    private LocalDateTime receivedAt;
    private Integer receivedQuantity;
    /** Required when recording a batch; user-entered delivery note number. */
    private String deliveryNoteNumber;
    /** Id of the delivery note created by the latest record call. */
    private Long deliveryNoteId;
    /** True when the order line is fully received after this batch. */
    private Boolean lineFullyReceived;
    private MaterialOrderReceptionInternalControlTO internalControl;
    private EUnitOfMeasure materialUnitOfMeasure;
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
    /** Whether the linked material order has an uploaded certificate. */
    private Boolean certificatePresent;
}
