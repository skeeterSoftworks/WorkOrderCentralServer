package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkSessionTO {

    private Long id;
    private Long workOrderId;
    private LocalDateTime sessionStart;
    private LocalDateTime sessionEnd;
    private long productCount;
    private String productReferenceID;
    private String operatorQrCode;
    private String operatorName;
    private String operatorSurname;
    private String stationId;
    /** True when this response follows reaching the work order production target (session auto-closed). */
    private boolean workOrderCompletedByTarget;
}
