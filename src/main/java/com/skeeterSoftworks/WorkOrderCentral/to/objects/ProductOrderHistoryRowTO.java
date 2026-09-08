package com.skeeterSoftworks.WorkOrderCentral.to.objects;

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
    private Long id;
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
}
