package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialOrderHistoryPageTO {
    private List<MaterialOrderHistoryRowTO> content = new ArrayList<>();
    private long totalElements;
    private int page;
    private int size;
}
