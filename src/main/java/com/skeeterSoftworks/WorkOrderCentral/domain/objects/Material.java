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
@ToString(exclude = { "providers" })
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

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "material_provider_link",
            joinColumns = @JoinColumn(name = "material_id"),
            inverseJoinColumns = @JoinColumn(name = "provider_id")
    )
    private java.util.List<MaterialProvider> providers = new java.util.ArrayList<>();

    @Column
    private float diameter;

    @Column
    private float weight;

    @Column
    private float length;

    @Column
    private float width;

}
