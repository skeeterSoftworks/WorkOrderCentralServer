package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QualityInfoStepTO {

    private Long id;
    private Integer stepNumber;
    private String stepDescription;
    /** Optional image payload (raw Base64 or data URL with comma prefix). */
    private String imageDataBase64;
}
