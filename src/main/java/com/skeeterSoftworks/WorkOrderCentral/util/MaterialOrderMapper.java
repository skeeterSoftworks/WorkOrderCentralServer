package com.skeeterSoftworks.WorkOrderCentral.util;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderLine;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EUnitOfMeasure;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.DeliveryNoteTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderLineTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class MaterialOrderMapper {

    private MaterialOrderMapper() {
    }

    public static MaterialOrderTO toTO(MaterialOrder order, Set<Long> receivedLineIds) {
        return toTO(order, receivedLineIds, Map.of());
    }

    public static MaterialOrderTO toTO(
            MaterialOrder order,
            Set<Long> receivedLineIds,
            Map<Long, LineDeliverySummary> deliveryByLineId) {
        MaterialOrderTO to = new MaterialOrderTO();
        to.setId(order.getId());
        to.setCode(order.getCode());
        if (order.getMaterialProvider() != null) {
            MaterialProvider provider = order.getMaterialProvider();
            to.setMaterialProviderId(provider.getId());
            to.setMaterialProviderName(provider.getName());
        }
        to.setStatus(order.getStatus());
        to.setLastChanged(order.getLastChanged());
        to.setCreatedAt(order.getCreatedAt());
        to.setRejectedAt(order.getRejectedAt());
        to.setCertificateBase64(null);
        to.setCertificatePresent(order.getCertificate() != null && order.getCertificate().length > 0);

        List<MaterialOrderLine> lines = order.getLines() != null ? order.getLines() : List.of();
        Set<Long> received = receivedLineIds != null ? receivedLineIds : Set.of();
        List<MaterialOrderLineTO> lineTos = lines.stream()
                .map(line -> lineToTO(
                        line,
                        received.contains(line.getId()),
                        deliveryByLineId.get(line.getId())))
                .collect(Collectors.toCollection(ArrayList::new));
        to.setLines(lineTos);

        int totalQuantity = lines.stream().mapToInt(MaterialOrderLine::getQuantity).sum();
        to.setQuantity(totalQuantity > 0 ? totalQuantity : null);

        if (lines.size() == 1) {
            applySingleLineSummary(to, lines.get(0));
        } else if (!lines.isEmpty()) {
            to.setMaterialName(lines.stream()
                    .map(line -> materialLabel(line.getMaterial()))
                    .filter(label -> !label.isBlank())
                    .collect(Collectors.joining(", ")));
            to.setMaterialCode(null);
            to.setMaterialId(null);
        }
        return to;
    }

    public static MaterialOrderLineTO lineToTO(MaterialOrderLine line, boolean received) {
        return lineToTO(line, received, null);
    }

    public static MaterialOrderLineTO lineToTO(
            MaterialOrderLine line,
            boolean received,
            LineDeliverySummary deliverySummary) {
        MaterialOrderLineTO to = new MaterialOrderLineTO();
        to.setId(line.getId());
        to.setQuantity(line.getQuantity());
        to.setReceived(received);
        if (deliverySummary != null) {
            to.setReceivedQuantityTotal(deliverySummary.receivedTotal());
            int remaining = line.getQuantity() - deliverySummary.receivedTotal();
            to.setRemainingQuantity(Math.max(0, remaining));
            to.setDeliveryNotes(deliverySummary.deliveryNotes());
        } else if (received) {
            to.setReceivedQuantityTotal(line.getQuantity());
            to.setRemainingQuantity(0);
            to.setDeliveryNotes(List.of());
        } else {
            to.setReceivedQuantityTotal(0);
            to.setRemainingQuantity(line.getQuantity());
            to.setDeliveryNotes(List.of());
        }
        Material material = line.getMaterial();
        if (material != null) {
            to.setMaterialId(material.getId());
            to.setMaterialName(material.getName());
            to.setMaterialCode(material.getCode());
            to.setMaterialUnitOfMeasure(material.getUnitOfMeasure() != null ? material.getUnitOfMeasure() : EUnitOfMeasure.PCS);
        }
        return to;
    }

    private static void applySingleLineSummary(MaterialOrderTO to, MaterialOrderLine line) {
        Material material = line.getMaterial();
        if (material == null) {
            return;
        }
        to.setMaterialId(material.getId());
        to.setMaterialName(material.getName());
        to.setMaterialCode(material.getCode());
        to.setMaterialUnitOfMeasure(material.getUnitOfMeasure() != null ? material.getUnitOfMeasure() : EUnitOfMeasure.PCS);
    }

    public static String materialLabel(Material material) {
        if (material == null) {
            return "";
        }
        if (material.getName() != null && !material.getName().isBlank()) {
            return material.getName().trim();
        }
        if (material.getCode() != null && !material.getCode().isBlank()) {
            return material.getCode().trim();
        }
        return "";
    }

    public static String linesSummary(MaterialOrder order) {
        if (order.getLines() == null || order.getLines().isEmpty()) {
            return "";
        }
        return order.getLines().stream()
                .map(line -> {
                    String label = materialLabel(line.getMaterial());
                    if (label.isBlank()) {
                        label = "material";
                    }
                    return label + " (" + line.getQuantity() + ")";
                })
                .collect(Collectors.joining(", "));
    }

    public static Set<Long> duplicateMaterialIds(List<MaterialOrderLineTO> lines) {
        Set<Long> seen = new HashSet<>();
        Set<Long> duplicates = new HashSet<>();
        if (lines == null) {
            return duplicates;
        }
        for (MaterialOrderLineTO line : lines) {
            if (line.getMaterialId() == null) {
                continue;
            }
            if (!seen.add(line.getMaterialId())) {
                duplicates.add(line.getMaterialId());
            }
        }
        return duplicates;
    }

    public record LineDeliverySummary(int receivedTotal, List<DeliveryNoteTO> deliveryNotes) {
    }
}
