package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMeasureCheckType;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMeasuringFeatureClassType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeasuringFeaturePrototypeTO {
    private Long id;
    private String catalogueId;
    private String description;
    private Boolean absoluteMeasure;
    private BigDecimal refValue;
    private BigDecimal minTolerance;
    private BigDecimal maxTolerance;
    private EMeasuringFeatureClassType classType;
    private String frequency;
    private EMeasureCheckType checkType;
    private String toolType;
    private String measuringTool;
}

