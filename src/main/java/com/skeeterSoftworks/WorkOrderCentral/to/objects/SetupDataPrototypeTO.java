package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetupDataPrototypeTO {
    private String operationID;
    private String toolID;
    private BigDecimal diameterRefValue;
    private BigDecimal diameterMaxPosTolerance;
    private BigDecimal diameterMaxNegTolerance;
    private BigDecimal heightRefValue;
    private BigDecimal heightMaxPosTolerance;
    private BigDecimal heightMaxNegTolerance;
    private Boolean attributiveHeightMeasurement;
    private Boolean attributiveDiameterMeasurement;
}
