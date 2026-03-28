package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Counts inserted by demo sample-data generation (admin API). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SampleDataGenerationResultTO {
    private int machinesInserted;
    private int toolsInserted;
    private int productsInserted;
    private int customersInserted;
    private int usersInserted;
}
