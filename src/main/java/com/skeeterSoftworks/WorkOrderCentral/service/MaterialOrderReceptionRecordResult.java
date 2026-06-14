package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.DeliveryNote;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderReception;

public record MaterialOrderReceptionRecordResult(
        DeliveryNote deliveryNote,
        MaterialOrderReception reception,
        boolean lineFullyReceived) {
}
