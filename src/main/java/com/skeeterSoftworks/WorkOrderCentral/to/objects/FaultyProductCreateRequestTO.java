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
    /** Number of faulty products to record with the same reason/cause/comment. Defaults to 1. */
    private Integer quantity;
}
