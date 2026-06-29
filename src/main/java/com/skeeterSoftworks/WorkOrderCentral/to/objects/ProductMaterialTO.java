package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EUnitOfMeasure;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductMaterialTO {
    private Long id;
    private Long materialId;
    private String materialName;
    private String materialCode;
    private Double quantityPerProductUnit;
    private EUnitOfMeasure unitOfMeasure;
}
