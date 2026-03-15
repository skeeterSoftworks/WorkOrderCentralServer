package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WorkOrderTO {
    private Long id;
    private Long purchaseOrderId;
    private LocalDate dueDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String comment;
}
