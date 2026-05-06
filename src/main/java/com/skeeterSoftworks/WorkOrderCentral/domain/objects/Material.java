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
@ToString(exclude = { "provider", "products" })
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String code;

    /**
     * How many products can be produced from one material unit.
     */
    @Column
    private Integer productsPerUnit;

    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "provider_id")
    private MaterialProvider provider;

    @Column
    private float diameter;

    @Column
    private float weight;

    @Column
    private float length;

    @Column
    private float width;

    @ManyToMany(mappedBy = "materials", fetch = FetchType.LAZY)
    private java.util.List<Product> products = new java.util.ArrayList<>();


}
