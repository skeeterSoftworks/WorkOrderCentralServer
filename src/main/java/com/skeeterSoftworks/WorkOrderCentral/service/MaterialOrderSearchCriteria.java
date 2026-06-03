package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMaterialOrderStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class MaterialOrderSearchCriteria {
    EMaterialOrderStatus status;
    LocalDate createdFrom;
    LocalDate createdTo;
    String code;
    String materialName;
    String materialProviderName;
    Integer quantity;
    LocalDate lastChangedFrom;
    LocalDate lastChangedTo;
    Boolean certificatePresent;
}
