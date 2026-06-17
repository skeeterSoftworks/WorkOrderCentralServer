package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.DeliveryNote;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderReception;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderReceptionInternalControl;
import com.skeeterSoftworks.WorkOrderCentral.service.MaterialOrderReceptionRecordResult;
import com.skeeterSoftworks.WorkOrderCentral.service.MaterialOrderReceptionService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderReceptionInternalControlTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderReceptionTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/material-order-receptions")
@CrossOrigin(origins = "*")
public class MaterialOrderReceptionFacade {

    private final MaterialOrderReceptionService materialOrderReceptionService;

    public MaterialOrderReceptionFacade(MaterialOrderReceptionService materialOrderReceptionService) {
        this.materialOrderReceptionService = materialOrderReceptionService;
    }

    @Transactional(readOnly = true)
    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            List<MaterialOrderReception> all = materialOrderReceptionService.getAll();
            return ResponseEntity.ok(all.stream().map(this::toTO).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_MATERIAL_ORDER_RECEPTIONS");
        }
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return materialOrderReceptionService.getById(id)
                    .map(r -> ResponseEntity.ok(toTO(r)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_MATERIAL_ORDER_RECEPTION");
        }
    }

    @Transactional(readOnly = true)
    @GetMapping("/pending-validation")
    public ResponseEntity<?> getPendingValidation() {
        try {
            List<MaterialOrderReception> list = materialOrderReceptionService.getPendingValidation();
            return ResponseEntity.ok(list.stream().map(this::toTO).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_PENDING_VALIDATION");
        }
    }

    @PostMapping("/{id}/submit-internal-control")
    public ResponseEntity<?> submitInternalControl(
            @PathVariable Long id,
            @RequestBody MaterialOrderReceptionInternalControlTO body) {
        try {
            MaterialOrderReception saved = materialOrderReceptionService.submitInternalControl(id, body);
            return ResponseEntity.ok(toTO(saved));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/record")
    public ResponseEntity<?> record(@RequestBody MaterialOrderReceptionTO body) {
        try {
            MaterialOrderReceptionRecordResult result = materialOrderReceptionService.recordReception(body);
            return ResponseEntity.ok(toRecordTO(result));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private MaterialOrderReceptionTO toRecordTO(MaterialOrderReceptionRecordResult result) {
        DeliveryNote note = result.deliveryNote();
        MaterialOrderReceptionTO to = toTO(result.reception());
        to.setDeliveryNoteId(note.getId());
        to.setDeliveryNoteNumber(note.getDeliveryNoteNumber());
        to.setReceivedAt(note.getReceivedAt());
        to.setReceivedQuantity(note.getQuantity());
        to.setLineFullyReceived(result.lineFullyReceived());
        return to;
    }

    private MaterialOrderReceptionTO toTO(MaterialOrderReception r) {
        MaterialOrderReceptionTO to = new MaterialOrderReceptionTO();
        to.setId(r.getId());
        to.setReceivedAt(r.getReceivedAt());
        to.setReceivedQuantity(r.getReceivedQuantity());
        DeliveryNote deliveryNote = r.getDeliveryNote();
        if (deliveryNote != null) {
            to.setDeliveryNoteId(deliveryNote.getId());
            to.setDeliveryNoteNumber(deliveryNote.getDeliveryNoteNumber());
            to.setReceivedAt(deliveryNote.getReceivedAt());
            to.setReceivedQuantity(deliveryNote.getQuantity());
        }
        MaterialOrder order = r.getMaterialOrder();
        if (order != null) {
            to.setMaterialOrderId(order.getId());
            to.setMaterialOrderCode(order.getCode());
            if (order.getMaterialProvider() != null) {
                to.setMaterialProviderName(order.getMaterialProvider().getName());
            }
            to.setCertificatePresent(MaterialOrderReceptionService.orderHasCertificate(order));
        }
        if (r.getMaterialOrderLine() != null) {
            to.setMaterialOrderLineId(r.getMaterialOrderLine().getId());
            Material material = r.getMaterialOrderLine().getMaterial();
            if (material != null) {
                to.setMaterialCode(material.getCode());
                to.setMaterialName(material.getName());
                to.setMaterialDiameter(material.getDiameter());
                to.setMaterialWeight(material.getWeight());
                to.setMaterialLength(material.getLength());
                to.setMaterialWidth(material.getWidth());
            }
        }
        to.setInternalControl(toInternalControlTO(r.getInternalControl()));
        return to;
    }

    private MaterialOrderReceptionInternalControlTO toInternalControlTO(MaterialOrderReceptionInternalControl ic) {
        if (ic == null) {
            return new MaterialOrderReceptionInternalControlTO();
        }
        MaterialOrderReceptionInternalControlTO to = new MaterialOrderReceptionInternalControlTO();
        to.setDiameterSamples(ic.getDiameterSamples() != null ? new ArrayList<>(ic.getDiameterSamples()) : new ArrayList<>());
        to.setLengthSamples(ic.getLengthSamples() != null ? new ArrayList<>(ic.getLengthSamples()) : new ArrayList<>());
        to.setWidthSamples(ic.getWidthSamples() != null ? new ArrayList<>(ic.getWidthSamples()) : new ArrayList<>());
        to.setWeightSamples(ic.getWeightSamples() != null ? new ArrayList<>(ic.getWeightSamples()) : new ArrayList<>());
        to.setOverallWeight(ic.getOverallWeight());
        to.setOverallAcceptance(ic.getOverallAcceptance());
        return to;
    }
}
