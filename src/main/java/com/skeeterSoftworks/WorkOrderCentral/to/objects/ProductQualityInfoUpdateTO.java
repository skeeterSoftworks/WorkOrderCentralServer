package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductQualityInfoUpdateTO {

    private Long machineId;
    private List<QualityInfoStepTO> qualityInfoSteps;
}
