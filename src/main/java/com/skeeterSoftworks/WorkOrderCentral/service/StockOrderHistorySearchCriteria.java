package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EStockOrderHistoryProductType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class StockOrderHistorySearchCriteria {
    EStockOrderHistoryProductType productType;
    LocalDate assignedFrom;
    LocalDate assignedTo;
    String assignedBy;
}
