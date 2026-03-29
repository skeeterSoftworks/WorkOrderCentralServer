package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetupProductTO {

    private Long id;
    private LocalDateTime recordedAt;
    private SetupDataPrototypeTO prototypeSnapshot;
    private String measuredHeight;
    private Boolean measuredHeightOk;
    private String measuredDiameter;
    private Boolean measuredDiameterOk;
}
