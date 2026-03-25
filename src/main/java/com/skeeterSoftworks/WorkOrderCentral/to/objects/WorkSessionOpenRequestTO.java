package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkSessionOpenRequestTO {

    private Long workOrderId;
    private String operatorQrCode;
    private String operatorName;
    private String operatorSurname;
    private String stationId;
}
