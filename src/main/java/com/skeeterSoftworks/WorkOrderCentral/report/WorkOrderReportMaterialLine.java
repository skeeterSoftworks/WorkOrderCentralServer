package com.skeeterSoftworks.WorkOrderCentral.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkOrderReportMaterialLine {

    private String materialCode;
    private String materialName;
    private String requiredQuantity;
}
