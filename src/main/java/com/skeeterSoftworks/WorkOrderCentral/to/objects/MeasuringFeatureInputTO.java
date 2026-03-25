package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeasuringFeatureInputTO {

    private String featureName;
    private Long width;
    private Long height;
    private Long depth;
    private Long diameter;
}
