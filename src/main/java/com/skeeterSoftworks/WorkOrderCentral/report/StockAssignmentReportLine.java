package com.skeeterSoftworks.WorkOrderCentral.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockAssignmentReportLine {
    private String locationCode;
    private int quantity;
}
