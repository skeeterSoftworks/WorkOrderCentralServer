package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EPurchaseOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PurchaseOrderTO {
    private Long id;
    private Long customerId;
    private CustomerTO customer;
    private List<ProductOrderTO> productOrderList;
    private EPurchaseOrderStatus orderStatus;
    private String currency;
    private LocalDate deliveryDate;
    private String deliveryTerms;
    private String shippingAddress;
    private String comment;
    /** Internal stock demand PO: produced goods on linked work orders count fully toward product stock. */
    private Boolean internalStockDemand;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime inProductionAt;
    private LocalDateTime completedAt;
    private LocalDateTime deliveredAt;
}

