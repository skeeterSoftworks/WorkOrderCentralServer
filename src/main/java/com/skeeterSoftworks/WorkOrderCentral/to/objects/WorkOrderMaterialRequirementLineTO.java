package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EUnitOfMeasure;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkOrderMaterialRequirementLineTO {
    private Long materialId;
    private String materialCode;
    private String materialName;
    private EUnitOfMeasure unitOfMeasure;
    private double requiredQuantity;
    private int availableQuantity;
    private double missingQuantity;
}
