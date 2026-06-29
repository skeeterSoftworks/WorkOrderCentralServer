package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EUnitOfMeasure;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EUnitOfMeasure unitOfMeasure = EUnitOfMeasure.PCS;

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "material_provider_link",
            joinColumns = @JoinColumn(name = "material_id"),
            inverseJoinColumns = @JoinColumn(name = "provider_id")
    )
    private java.util.List<MaterialProvider> providers = new java.util.ArrayList<>();
}
