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
@ToString
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String productGroup;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private Long stockQuantity;

    @Column
    private String machineType;

    @Column
    private String toolType;

}
