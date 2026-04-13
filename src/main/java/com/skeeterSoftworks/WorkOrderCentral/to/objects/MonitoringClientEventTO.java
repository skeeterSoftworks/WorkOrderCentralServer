package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringClientEventTO {

    private String eventType;
    private Long machineId;
    private String machineName;
    private Long workSessionId;
    private Long workOrderId;
    private Long goodProductsCount;
    private String details;
    private LocalDateTime timestamp;
}
