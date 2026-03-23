package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeasuringFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "control_product_id")
    private ControlProduct controlProduct;

    @Column
    private String featureName;

    @Column
    private Long width;

    @Column
    private Long height;

    @Column
    private Long depth;

    @Column
    private Long diameter;
}
