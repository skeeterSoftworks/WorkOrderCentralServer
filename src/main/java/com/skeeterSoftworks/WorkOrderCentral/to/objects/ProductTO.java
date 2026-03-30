package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductTO {
    private Long id;
    private String name;
    private String description;
    /** Catalogue / reference ID for the product. */
    private String reference;
    private List<Long> machineIds;
    private List<Long> customerIds;
    private Long toolId;
    private SetupDataPrototypeTO setupDataPrototype;
    private List<MeasuringFeaturePrototypeTO> measuringFeaturePrototypes;
    private List<QualityInfoStepTO> qualityInfoSteps;
    /** Optional technical drawing image (raw Base64 or data URL). */
    private String technicalDrawingBase64;
}

