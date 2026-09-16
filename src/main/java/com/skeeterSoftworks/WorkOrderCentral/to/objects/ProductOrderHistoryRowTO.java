package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EOrdersHistoryEventType;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EPurchaseOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductOrderHistoryRowTO {
    /** Stable UI key, e.g. ORDER-12, STOCK_IN-3, STOCK_OUT-SAO-5. */
    private String rowKey;
    private EOrdersHistoryEventType eventType;
    /** Sort / display timestamp for the event. */
    private LocalDateTime eventAt;
    private Long id;
    /** @deprecated prefer {@link #eventAt}; kept for compatibility with older clients. */
    private LocalDateTime orderedAt;
    private Long purchaseOrderId;
    private String purchaseOrderCode;
    private EPurchaseOrderStatus orderStatus;
    private Long productId;
    private String productReference;
    private String productName;
    private Long customerId;
    private String customerName;
    private String buyerId;
    private Integer quantity;
    private BigDecimal pricePerUnit;
    private String currency;
    private Long workOrderId;
    private String workOrderCode;
    private String actorFullName;
}
