package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorTO {
    /** Client i18n key (e.g. errorMachineDeleteLinkedProducts). */
    private String code;
    /** Interpolation values for the translated message. */
    private Map<String, Object> params;
}
