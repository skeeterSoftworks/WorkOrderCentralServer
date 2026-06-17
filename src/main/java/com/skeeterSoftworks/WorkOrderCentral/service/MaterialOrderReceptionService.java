package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderLine;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderReception;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderReceptionInternalControl;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.DeliveryNote;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.DeliveryNoteRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialOrderReceptionRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMaterialOrderStatus;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderReceptionInternalControlTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderReceptionTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class MaterialOrderReceptionService {

    private static final int REQUIRED_SAMPLES = 3;

    private final MaterialOrderReceptionRepository materialOrderReceptionRepository;
    private final MaterialOrderRepository materialOrderRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final StockInventoryService stockInventoryService;

    public MaterialOrderReceptionService(
            MaterialOrderReceptionRepository materialOrderReceptionRepository,
            MaterialOrderRepository materialOrderRepository,
            DeliveryNoteRepository deliveryNoteRepository,
            StockInventoryService stockInventoryService) {
        this.materialOrderReceptionRepository = materialOrderReceptionRepository;
        this.materialOrderRepository = materialOrderRepository;
        this.deliveryNoteRepository = deliveryNoteRepository;
        this.stockInventoryService = stockInventoryService;
    }

    public List<MaterialOrderReception> getAll() {
        return materialOrderReceptionRepository.findAll();
    }

    public Optional<MaterialOrderReception> getById(Long id) {
        return materialOrderReceptionRepository.findById(id);
    }

    public List<MaterialOrderReception> getByMaterialOrderId(Long materialOrderId) {
        return materialOrderReceptionRepository.findByMaterialOrder_Id(materialOrderId);
    }

    public List<MaterialOrderReception> getPendingValidation() {
        return materialOrderReceptionRepository.findAll().stream()
                .filter(r -> r.getMaterialOrder() != null
                        && (r.getInternalControl() == null
                        || r.getInternalControl().getOverallAcceptance() == null))
                .sorted(Comparator.comparing(MaterialOrderReception::getReceivedAt).reversed())
                .toList();
    }

    @Transactional
    public MaterialOrderReception submitInternalControl(Long receptionId, MaterialOrderReceptionInternalControlTO body)
            throws Exception {
        MaterialOrderReception reception = materialOrderReceptionRepository.findById(receptionId)
                .orElseThrow(() -> new Exception("MATERIAL_ORDER_RECEPTION_NOT_FOUND"));
        MaterialOrder order = reception.getMaterialOrder();
        if (order == null) {
            throw new Exception("MATERIAL_ORDER_NOT_FOUND");
        }
        if (order.getStatus() == EMaterialOrderStatus.REJECTED) {
            throw new Exception("MATERIAL_ORDER_NOT_PENDING_VALIDATION");
        }
        if (reception.getInternalControl() != null
                && reception.getInternalControl().getOverallAcceptance() != null) {
            throw new Exception("MATERIAL_ORDER_RECEPTION_ALREADY_VALIDATED");
        }

        MaterialOrderLine line = reception.getMaterialOrderLine();
        if (line == null) {
            throw new Exception("MATERIAL_ORDER_LINE_NOT_FOUND");
        }
        Material material = line.getMaterial();
        if (material == null) {
            throw new Exception("MATERIAL_NOT_FOUND");
        }

        MaterialOrderReceptionInternalControl ic = reception.getInternalControl();
        if (ic == null) {
            ic = new MaterialOrderReceptionInternalControl();
            reception.setInternalControl(ic);
        }

        if (isDimensionDefined(material.getDiameter())) {
            applyOptionalSamples(ic::setDiameterSamples, body != null ? body.getDiameterSamples() : null);
        }
        if (isDimensionDefined(material.getLength())) {
            applyOptionalSamples(ic::setLengthSamples, body != null ? body.getLengthSamples() : null);
        }
        if (isDimensionDefined(material.getWidth())) {
            applyOptionalSamples(ic::setWidthSamples, body != null ? body.getWidthSamples() : null);
        }

        if (body == null || body.getOverallAcceptance() == null) {
            throw new Exception("MATERIAL_ORDER_RECEPTION_OVERALL_ACCEPTANCE_REQUIRED");
        }
        if (body.getOverallWeight() != null && Float.isFinite(body.getOverallWeight())) {
            ic.setOverallWeight(body.getOverallWeight());
        }
        ic.setOverallAcceptance(body.getOverallAcceptance());

        MaterialOrderReception saved = materialOrderReceptionRepository.save(reception);
        refreshOrderValidationStatus(order);
        return saved;
    }

    private void refreshOrderValidationStatus(MaterialOrder order) {
        Set<Long> receivedLineIds = deliveryNoteRepository.findFullyReceivedLineIdsByMaterialOrderId(order.getId());
        boolean allReceived = order.getLines() != null
                && !order.getLines().isEmpty()
                && order.getLines().stream().allMatch(line -> receivedLineIds.contains(line.getId()));
        if (!allReceived) {
            return;
        }
        List<DeliveryNote> deliveryNotes =
                deliveryNoteRepository.findByMaterialOrder_IdOrderByReceivedAtDescIdDesc(order.getId());
        if (deliveryNotes.isEmpty()) {
            return;
        }
        List<MaterialOrderReception> receptions = materialOrderReceptionRepository.findByMaterialOrder_Id(order.getId());
        boolean allBatchesValidated = deliveryNotes.stream().allMatch(note ->
                receptions.stream().anyMatch(reception ->
                        reception.getDeliveryNote() != null
                                && reception.getDeliveryNote().getId() == note.getId()
                                && reception.getInternalControl() != null
                                && reception.getInternalControl().getOverallAcceptance() != null));
        if (allBatchesValidated) {
            order.setStatus(EMaterialOrderStatus.VALIDATED);
        } else {
            order.setStatus(EMaterialOrderStatus.RECEIVED_IN_STOCK);
        }
        order.setLastChanged(LocalDateTime.now());
        materialOrderRepository.save(order);
    }

    private static boolean isDimensionDefined(float value) {
        return value != 0f;
    }

    private static void applyOptionalSamples(Consumer<List<Float>> setter, List<Float> samples) {
        if (samples == null || samples.isEmpty()) {
            return;
        }
        if (samples.size() < REQUIRED_SAMPLES) {
            return;
        }
        List<Float> parsed = samples.stream()
                .limit(REQUIRED_SAMPLES)
                .collect(Collectors.toCollection(ArrayList::new));
        for (Float sample : parsed) {
            if (sample == null || !Float.isFinite(sample)) {
                return;
            }
        }
        setter.accept(parsed);
    }

    @Transactional
    public MaterialOrderReceptionRecordResult recordReception(MaterialOrderReceptionTO to) throws Exception {
        if (to == null || to.getMaterialOrderId() == null || to.getMaterialOrderId() <= 0) {
            throw new Exception("MATERIAL_ORDER_RECEPTION_ORDER_REQUIRED");
        }
        if (to.getReceivedAt() == null) {
            throw new Exception("MATERIAL_ORDER_RECEPTION_DATE_REQUIRED");
        }
        if (!StringUtils.hasText(to.getDeliveryNoteNumber())) {
            throw new Exception("DELIVERY_NOTE_NUMBER_REQUIRED");
        }
        if (to.getReceivedQuantity() == null || to.getReceivedQuantity() <= 0) {
            throw new Exception("MATERIAL_ORDER_RECEPTION_INVALID_QUANTITY");
        }

        MaterialOrder order = materialOrderRepository.findById(to.getMaterialOrderId())
                .orElseThrow(() -> new Exception("MATERIAL_ORDER_NOT_FOUND"));

        if (order.getStatus() == EMaterialOrderStatus.VALIDATED) {
            throw new Exception("MATERIAL_ORDER_ALREADY_RECEIVED");
        }
        if (order.getStatus() != EMaterialOrderStatus.IN_TRANSPORT
                && order.getStatus() != EMaterialOrderStatus.RECEIVED_IN_STOCK) {
            throw new Exception("MATERIAL_ORDER_NOT_OPEN_FOR_RECEPTION");
        }

        MaterialOrderLine line = resolveLine(order, to.getMaterialOrderLineId());
        int alreadyReceived = deliveryNoteRepository.sumQuantityByMaterialOrderLineId(line.getId());
        int remaining = line.getQuantity() - alreadyReceived;
        if (remaining <= 0) {
            throw new Exception("MATERIAL_ORDER_LINE_ALREADY_RECEIVED");
        }
        if (to.getReceivedQuantity() > remaining) {
            throw new Exception("MATERIAL_ORDER_RECEPTION_QUANTITY_EXCEEDS_REMAINING");
        }
        if (line.getMaterial() == null || line.getMaterial().getId() == null) {
            throw new Exception("MATERIAL_NOT_FOUND");
        }

        DeliveryNote deliveryNote = new DeliveryNote();
        deliveryNote.setMaterialOrder(order);
        deliveryNote.setMaterialOrderLine(line);
        deliveryNote.setDeliveryNoteNumber(to.getDeliveryNoteNumber().trim());
        deliveryNote.setReceivedAt(to.getReceivedAt());
        deliveryNote.setQuantity(to.getReceivedQuantity());
        DeliveryNote savedNote = deliveryNoteRepository.save(deliveryNote);

        stockInventoryService.applyReceptionStockAllocations(
                line.getMaterial(),
                to.getReceivedQuantity(),
                to.getStockAllocations());

        int receivedAfter = alreadyReceived + to.getReceivedQuantity();
        boolean lineFullyReceived = receivedAfter >= line.getQuantity();

        MaterialOrderReception reception = new MaterialOrderReception();
        reception.setMaterialOrder(order);
        reception.setMaterialOrderLine(line);
        reception.setDeliveryNote(savedNote);
        reception.setReceivedAt(to.getReceivedAt());
        reception.setReceivedQuantity(to.getReceivedQuantity());
        reception.setInternalControl(new MaterialOrderReceptionInternalControl());
        reception = materialOrderReceptionRepository.save(reception);

        refreshOrderReceptionStatus(order);
        refreshOrderValidationStatus(order);
        return new MaterialOrderReceptionRecordResult(savedNote, reception, lineFullyReceived);
    }

    private MaterialOrderLine resolveLine(MaterialOrder order, Long materialOrderLineId) throws Exception {
        List<MaterialOrderLine> lines = order.getLines() != null ? order.getLines() : List.of();
        if (lines.isEmpty()) {
            throw new Exception("MATERIAL_ORDER_LINES_REQUIRED");
        }
        if (materialOrderLineId != null && materialOrderLineId > 0) {
            return lines.stream()
                    .filter(line -> line.getId() == materialOrderLineId)
                    .findFirst()
                    .orElseThrow(() -> new Exception("MATERIAL_ORDER_LINE_NOT_FOUND"));
        }
        if (lines.size() == 1) {
            return lines.get(0);
        }
        throw new Exception("MATERIAL_ORDER_RECEPTION_LINE_REQUIRED");
    }

    private void refreshOrderReceptionStatus(MaterialOrder order) {
        Set<Long> receivedLineIds = deliveryNoteRepository.findFullyReceivedLineIdsByMaterialOrderId(order.getId());
        boolean allReceived = order.getLines() != null
                && !order.getLines().isEmpty()
                && order.getLines().stream().allMatch(line -> receivedLineIds.contains(line.getId()));
        if (allReceived) {
            order.setStatus(EMaterialOrderStatus.RECEIVED_IN_STOCK);
        } else {
            order.setStatus(EMaterialOrderStatus.IN_TRANSPORT);
        }
        order.setLastChanged(LocalDateTime.now());
        materialOrderRepository.save(order);
    }

    public static boolean orderHasCertificate(MaterialOrder order) {
        return order != null && order.getCertificate() != null && order.getCertificate().length > 0;
    }
}
