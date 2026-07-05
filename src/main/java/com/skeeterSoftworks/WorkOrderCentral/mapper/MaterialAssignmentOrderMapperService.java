package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialAssignmentOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialAssignmentOrderLine;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductOrder;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialAssignmentOrderLineTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialAssignmentOrderTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MaterialAssignmentOrderMapperService {

    public MaterialAssignmentOrderTO mapToTO(MaterialAssignmentOrder order) {
        if (order == null) {
            return null;
        }
        MaterialAssignmentOrderTO to = new MaterialAssignmentOrderTO();
        to.setId(order.getId());
        to.setCode(order.getCode());
        if (order.getWorkOrder() != null) {
            to.setWorkOrderId(order.getWorkOrder().getId());
            ProductOrder line = order.getWorkOrder().getProductOrder();
            if (line != null && line.getProduct() != null) {
                to.setProductReference(line.getProduct().getReference());
                to.setProductName(line.getProduct().getName());
            }
        }
        to.setStatus(order.getStatus());
        to.setCreatedAt(order.getCreatedAt());
        to.setCreatedByFullName(order.getCreatedByFullName());
        to.setAssignedAt(order.getAssignedAt());
        to.setAssignedByUserQr(order.getAssignedByUserQr());
        to.setAssignedByFullName(order.getAssignedByFullName());
        to.setLines(mapLines(order.getLines()));
        return to;
    }

    private List<MaterialAssignmentOrderLineTO> mapLines(List<MaterialAssignmentOrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            return List.of();
        }
        List<MaterialAssignmentOrderLineTO> result = new ArrayList<>();
        for (MaterialAssignmentOrderLine line : lines) {
            if (line == null) {
                continue;
            }
            MaterialAssignmentOrderLineTO row = new MaterialAssignmentOrderLineTO();
            Material material = line.getMaterial();
            if (material != null) {
                row.setMaterialId(material.getId());
                row.setMaterialCode(material.getCode());
                row.setMaterialName(material.getName());
            }
            row.setQuantity(line.getQuantity());
            result.add(row);
        }
        return result;
    }
}
