package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EUnitOfMeasure;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"materialOrder", "material"})
public class MaterialOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "material_order_id", nullable = false)
    private MaterialOrder materialOrder;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(nullable = false)
    private int quantity;

    /** Optional unit price when the order was placed (requested). */
    @Column(precision = 19, scale = 4)
    private BigDecimal pricePerUnit;

    /** Optional unit price offered/accepted by the provider. */
    @Column(precision = 19, scale = 4)
    private BigDecimal offeredPricePerUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EUnitOfMeasure unitOfMeasure = EUnitOfMeasure.PCS;
}
