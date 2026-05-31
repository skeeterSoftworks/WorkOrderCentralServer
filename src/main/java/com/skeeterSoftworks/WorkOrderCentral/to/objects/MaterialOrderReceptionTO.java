package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialOrderReceptionTO {
    private Long id;
    private Long materialOrderId;
    private String materialOrderCode;
    private String materialCode;
    private String materialName;
    private String materialProviderName;
    private LocalDateTime receivedAt;
    private Integer receivedQuantity;
    private MaterialOrderReceptionInternalControlTO internalControl;
}
