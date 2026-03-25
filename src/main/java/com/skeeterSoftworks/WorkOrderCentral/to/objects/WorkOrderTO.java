package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EWorkOrderState;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WorkOrderTO {
    private Long id;
    /** Purchase order line this work order belongs to (required for create/update). */
    private Long productOrderId;
    /** Denormalized for UI: parent purchase order id. */
    private Long purchaseOrderId;
    /** Denormalized for UI. */
    private String productName;
    /** Product catalogue / reference id (denormalized). */
    private String productReference;
    private LocalDate dueDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String comment;
    private Integer requiredQuantity;
    private Long producedGoodQuantity;
    private EWorkOrderState state;
}
