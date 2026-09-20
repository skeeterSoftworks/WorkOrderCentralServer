package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnologyToolHistoryRowTO {
    private String rowKey;
    private Long toolUsageId;
    private Long workSessionId;
    private LocalDateTime sessionStartedAt;
    private LocalDateTime sessionEndedAt;
    private Long sessionProductCount;
    private Long sessionFaultyProductCount;
    private Long workOrderId;
    private String workOrderCode;
    private String productReference;
    private String productName;
    private String stationId;
    private String operatorName;
    private String operatorSurname;
    private String cycleTime;
    private Integer norm100;
    private Integer piecesPerMaterial;
    private Long sourceToolId;
    private String toolName;
    private String toolDescription;
    private Integer orderNumber;
    private Integer workingTime;
}
