package com.skeeterSoftworks.WorkOrderCentral.domain.objects;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = { "machines", "customers", "tool", "measuringFeaturePrototypes", "setupDataPrototype", "qualityInfoSteps", "technicalDrawing" })
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String productGroup;

    @Column
    private String name;

    @Column
    private String description;

    /** Catalogue / reference ID for this product (not tied to a specific purchase order). */
    @Column
    private String reference;

    @Column
    private Long stockQuantity;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column
    private byte[] technicalDrawing;

    @ManyToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 32)
    @JoinTable(
            name = "product_machine",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "machine_id")
    )
    private List<Machine> machines = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 32)
    @JoinTable(
            name = "product_customer",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "customer_id")
    )
    private List<Customer> customers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_id")
    private Tool tool;

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @BatchSize(size = 32)
    private List<MeasuringFeaturePrototype> measuringFeaturePrototypes = new ArrayList<>();

    @Embedded
    private SetupDataPrototype setupDataPrototype;

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<QualityInfoStep> qualityInfoSteps = new ArrayList<>();

}
