package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Optional payload when recording a setup / tool-change event for a work session. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkSessionSetupProductCreateTO {
    private String measuredHeight;
    private Boolean measuredHeightOk;
    private String measuredDiameter;
    private Boolean measuredDiameterOk;
}
