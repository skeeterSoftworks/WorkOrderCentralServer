package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(
        name = "product_material",
        uniqueConstraints = @UniqueConstraint(columnNames = { "product_id", "material_id" })
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = { "product", "material" })
public class ProductMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    /**
     * Material quantity consumed to produce one product unit.
     */
    @Column(nullable = false)
    private double quantityPerProductUnit = 1d;
}
