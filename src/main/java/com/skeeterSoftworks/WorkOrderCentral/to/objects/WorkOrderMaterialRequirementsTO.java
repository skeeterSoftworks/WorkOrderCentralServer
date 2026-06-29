package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkOrderMaterialRequirementsTO {
    private Long productId;
    private String productReference;
    private String productName;
    private int productQuantity;
    private boolean hasBillOfMaterials;
    private boolean fullyAvailable;
    private List<WorkOrderMaterialRequirementLineTO> lines = new ArrayList<>();
}
