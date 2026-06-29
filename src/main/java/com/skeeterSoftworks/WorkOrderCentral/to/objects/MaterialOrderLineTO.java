package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EUnitOfMeasure;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialOrderLineTO {
    private Long id;
    private Long materialId;
    private String materialName;
    private String materialCode;
    private Integer quantity;
    /** Whether this line has been fully received in stock. */
    private Boolean received;
    /** Sum of quantities on all delivery notes for this line. */
    private Integer receivedQuantityTotal;
    /** Order line quantity minus received total (0 when fully received). */
    private Integer remainingQuantity;
    /** Delivery notes recorded for this line. */
    private java.util.List<DeliveryNoteTO> deliveryNotes;
    private EUnitOfMeasure materialUnitOfMeasure;
    /** Nominal diameter from linked material (0 = not defined). */
    private Float materialDiameter;
    /** Nominal weight from linked material (0 = not defined). */
    private Float materialWeight;
    /** Nominal length from linked material (0 = not defined). */
    private Float materialLength;
    /** Nominal width from linked material (0 = not defined). */
    private Float materialWidth;
}
