package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ControlProductTO {
    private Long id;
    private LocalDateTime createdAt;
    private List<ControlMeasuringFeatureTO> measuringFeatures = new ArrayList<>();
}
