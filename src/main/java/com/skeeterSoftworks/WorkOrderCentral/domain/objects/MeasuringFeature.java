package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMeasureCheckType;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMeasuringFeatureClassType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
    private String catalogueId;

    @Column
    private String description;

    @Column(columnDefinition="Decimal(10,5)")
    private BigDecimal refValue;

    @Column(columnDefinition="Decimal(10,5)")
    private BigDecimal minTolerance;

    @Column(columnDefinition="Decimal(10,5)")
    private BigDecimal maxTolerance;

    @Enumerated(EnumType.STRING)
    @Column
    private EMeasuringFeatureClassType classType;

    @Column
    private String frequency;

    @Enumerated(EnumType.STRING)
    @Column
    private EMeasureCheckType checkType;

    @Column
    private String toolType;

    @Column
    private String measuringTool;

    @Column
    private String assessedValue;

    @Column
    private boolean assessedValueGood;
}
