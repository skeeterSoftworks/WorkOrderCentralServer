package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialOrderAcceptTO {
    private List<MaterialOrderAcceptLineTO> lines = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialOrderAcceptLineTO {
        private Long lineId;
        private BigDecimal offeredPricePerUnit;
    }
}
