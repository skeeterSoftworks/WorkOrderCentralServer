package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FulfillStockAssignmentOrderRequestTO {
    private String code;
    /** Optional logged-in operator QR from stock-local session. */
    private String operatorUserQrCode;
}
