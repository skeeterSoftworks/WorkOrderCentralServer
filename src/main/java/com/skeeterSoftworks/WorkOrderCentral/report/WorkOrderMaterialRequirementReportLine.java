package com.skeeterSoftworks.WorkOrderCentral.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkOrderMaterialRequirementReportLine {
    private String materialCode;
    private String materialName;
    private String unitOfMeasure;
    private String requiredQuantity;
    private String availableQuantity;
    private String missingQuantity;
}
