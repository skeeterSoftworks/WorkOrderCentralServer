package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectOptionsTO {

    private List<String> measuringTools = new ArrayList<>();
    private List<String> deliveryTerms = new ArrayList<>();
    private List<String> rejectCauses = new ArrayList<>();
}
