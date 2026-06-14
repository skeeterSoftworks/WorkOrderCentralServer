package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryNoteTO {
    private Long id;
    private Long materialOrderId;
    private Long materialOrderLineId;
    private String deliveryNoteNumber;
    private LocalDateTime receivedAt;
    private Integer quantity;
}
