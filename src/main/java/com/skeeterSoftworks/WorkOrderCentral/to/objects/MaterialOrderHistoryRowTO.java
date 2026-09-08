package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMaterialOrderStatus;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EUnitOfMeasure;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialOrderHistoryRowTO {
    private Long id;
    private LocalDateTime orderedAt;
    private Long materialOrderId;
    private String materialOrderCode;
    private EMaterialOrderStatus status;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private Long materialProviderId;
    private String materialProviderName;
    private Integer quantity;
    private EUnitOfMeasure unitOfMeasure;
}
