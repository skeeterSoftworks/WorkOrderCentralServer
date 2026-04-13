package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TechnologyTO {

    private Long id;
    private String cycleTime;
    private Integer norm100;
    private Integer piecesPerMaterial;
    private List<ToolTO> tools = new ArrayList<>();

}
