package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EStockAssignmentOrderStatus;
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
    /** Denormalized: true when parent PO is internal stock demand. */
    private Boolean internalStockDemand;
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
    /** Optional stock allocations when creating a work order (create only). */
    private java.util.List<WorkOrderStockAllocationTO> stockAssignments;
    /** Logged-in user QR code (create only); shown as creator on stock assignment PDF. */
    private String createdByUserQrCode;
    /** Denormalized: 8-digit stock assignment order code when present. */
    private String stockAssignmentOrderCode;
    /** Denormalized: stock assignment order fulfillment status. */
    private EStockAssignmentOrderStatus stockAssignmentOrderStatus;
}
