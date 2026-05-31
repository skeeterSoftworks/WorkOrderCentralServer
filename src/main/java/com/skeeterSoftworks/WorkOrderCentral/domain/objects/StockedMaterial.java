package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = { "stockLocation", "material" })
public class StockedMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_location_id", nullable = false)
    private StockLocation stockLocation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(nullable = false)
    private int quantity = 0;
}
