package com.skeeterSoftworks.WorkOrderCentral.domain.objects;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EPurchaseOrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    @JsonManagedReference
    private List<ProductOrder> productOrderList;

    @Column
    private EPurchaseOrderStatus orderStatus;

    @Column
    private String currency;

    @Column
    private LocalDate deliveryDate;

    @Column
    private String reference;

    @Column
    private String deliveryTerms;

    @Column
    private String shippingAddress;

    @Column
    private String comment;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime confirmedAt;

    @Column
    private LocalDateTime inProductionAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private LocalDateTime deliveredAt;

}
