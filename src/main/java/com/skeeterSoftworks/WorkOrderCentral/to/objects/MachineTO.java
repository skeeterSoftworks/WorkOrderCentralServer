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
public class MachineTO {
    private Long id;
    private String machineName;
    private Long cycleTime;
    private String barLocation;
    private Long piecesPerBar;
    private Long barsPerSeries;
    private Long barsCount;
    private Double weightPerBar;
    private Double sumBarWeight;
    private String seriesID;
    private List<ToolTO> tools = new ArrayList<>();
}
