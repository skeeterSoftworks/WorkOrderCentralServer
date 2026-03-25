package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaultyProductCreateRequestTO {

    private String rejectReason;
    private String rejectCause;
    private String rejectComment;
}
