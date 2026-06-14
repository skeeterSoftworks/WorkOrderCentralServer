package com.skeeterSoftworks.WorkOrderCentral.to.objects;

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
    /** Nominal diameter from linked material (0 = not defined). */
    private Float materialDiameter;
    /** Nominal weight from linked material (0 = not defined). */
    private Float materialWeight;
    /** Nominal length from linked material (0 = not defined). */
    private Float materialLength;
    /** Nominal width from linked material (0 = not defined). */
    private Float materialWidth;
}
