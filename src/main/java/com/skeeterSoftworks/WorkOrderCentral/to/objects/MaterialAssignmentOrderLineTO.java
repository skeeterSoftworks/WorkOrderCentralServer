package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialAssignmentOrderLineTO {
    private Long materialId;
    private String materialCode;
    private String materialName;
    private Integer quantity;
}
