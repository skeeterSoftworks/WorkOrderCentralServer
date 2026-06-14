package com.skeeterSoftworks.WorkOrderCentral.util;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.DeliveryNote;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.DeliveryNoteTO;

public final class DeliveryNoteMapper {

    private DeliveryNoteMapper() {
    }

    public static DeliveryNoteTO toTO(DeliveryNote note) {
        DeliveryNoteTO to = new DeliveryNoteTO();
        to.setId(note.getId());
        if (note.getMaterialOrder() != null) {
            to.setMaterialOrderId(note.getMaterialOrder().getId());
        }
        if (note.getMaterialOrderLine() != null) {
            to.setMaterialOrderLineId(note.getMaterialOrderLine().getId());
        }
        to.setDeliveryNoteNumber(note.getDeliveryNoteNumber());
        to.setReceivedAt(note.getReceivedAt());
        to.setQuantity(note.getQuantity());
        return to;
    }
}
