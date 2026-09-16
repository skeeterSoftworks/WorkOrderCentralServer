package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMaterialOrderStatus;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EOrdersHistoryEventType;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EUnitOfMeasure;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialOrderHistoryRowTO {
    /** Stable UI key, e.g. ORDER-12, STOCK_IN-3. */
    private String rowKey;
    private EOrdersHistoryEventType eventType;
    /** Sort / display timestamp for the event. */
    private LocalDateTime eventAt;
    private Long id;
    /** @deprecated prefer {@link #eventAt}; kept for compatibility with older clients. */
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
    private BigDecimal pricePerUnit;
    private EUnitOfMeasure unitOfMeasure;
    private String deliveryNoteNumber;
}
