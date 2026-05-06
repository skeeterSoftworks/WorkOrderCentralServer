package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = { "materials" })
public class MaterialProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String contactPerson;

    @Column
    private String emailAddress;

    @Column
    private String phoneNumber;

    /**
     * Supplier grade from 1 (worst) to 5 (best); 0 means ungraded.
     */
    @Column(nullable = false)
    private int grade = 0;

    @OneToMany(mappedBy = "provider", fetch = FetchType.LAZY)
    private List<Material> materials = new ArrayList<>();
}
