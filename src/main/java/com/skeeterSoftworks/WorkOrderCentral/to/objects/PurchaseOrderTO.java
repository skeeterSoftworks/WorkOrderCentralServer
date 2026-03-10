package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EPurchaseOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PurchaseOrderTO {
    private Long id;
    private CustomerTO customer;
    private List<ProductOrderTO> productOrderList;
    private EPurchaseOrderStatus orderStatus;
    private String currency;
    private LocalDate deliveryDate;
    private String reference;
    private String deliveryTerms;
    private String shippingAddress;
    private String comment;
}

