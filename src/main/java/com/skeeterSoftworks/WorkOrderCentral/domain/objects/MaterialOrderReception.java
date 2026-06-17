package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"materialOrder"})
public class MaterialOrderReception {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "material_order_id", nullable = false)
    private MaterialOrder materialOrder;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "material_order_line_id", nullable = false)
    private MaterialOrderLine materialOrderLine;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_note_id", unique = true)
    private DeliveryNote deliveryNote;

    @Column(nullable = false)
    private LocalDateTime receivedAt;

    @Column(nullable = false)
    private int receivedQuantity;

    @Embedded
    private MaterialOrderReceptionInternalControl internalControl;
}
