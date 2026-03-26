package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeasuringFeatureInputTO {

    /** Links the submitted assessment to a specific measuring feature prototype (by catalogue id). */
    private String catalogueId;

    /** Digits-only for MEASURED features. Stored into {@code MeasuringFeature.assessedValue}. */
    private String assessedValue;

    /** Used for ATTRIBUTIVE features. Stored into {@code MeasuringFeature.assessedValueGood}. */
    private boolean assessedValueGood;
}
