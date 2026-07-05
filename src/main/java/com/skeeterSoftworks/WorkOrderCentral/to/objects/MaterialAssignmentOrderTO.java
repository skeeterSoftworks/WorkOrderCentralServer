package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EStockAssignmentOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialAssignmentOrderTO {
    private Long id;
    private String code;
    private Long workOrderId;
    private String productReference;
    private String productName;
    private EStockAssignmentOrderStatus status;
    private LocalDateTime createdAt;
    private String createdByFullName;
    private LocalDateTime assignedAt;
    private String assignedByUserQr;
    private String assignedByFullName;
    private List<MaterialAssignmentOrderLineTO> lines = new ArrayList<>();
}
