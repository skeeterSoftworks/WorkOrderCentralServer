package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialTO {
    private Long id;
    private String name;
    private String code;
    private Integer productsPerUnit;
    private Float diameter;
    private Float weight;
    private Float length;
    private Float width;
    private MaterialProviderTO provider;
}
