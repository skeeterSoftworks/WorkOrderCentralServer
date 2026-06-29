package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EUnitOfMeasure;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialTO {
    private Long id;
    private String name;
    private String code;
    private EUnitOfMeasure unitOfMeasure;
    private List<MaterialProviderTO> providers;
}
