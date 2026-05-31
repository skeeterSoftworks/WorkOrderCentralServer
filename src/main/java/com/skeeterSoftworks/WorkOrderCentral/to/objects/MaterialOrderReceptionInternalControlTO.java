package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialOrderReceptionInternalControlTO {
    private List<Float> diameterSamples = new ArrayList<>();
    private List<Float> lengthSamples = new ArrayList<>();
    private List<Float> widthSamples = new ArrayList<>();
    private List<Float> weightSamples = new ArrayList<>();
    private Float overallWeight;
    private Boolean overallAcceptance;
}
