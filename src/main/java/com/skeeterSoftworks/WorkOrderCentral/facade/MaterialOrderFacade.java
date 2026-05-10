package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import com.skeeterSoftworks.WorkOrderCentral.service.MaterialOrderService;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMaterialOrderStatus;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderStatusTransitionTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/material-orders")
@CrossOrigin(origins = "*")
public class MaterialOrderFacade {

    private final MaterialOrderService materialOrderService;

    public MaterialOrderFacade(MaterialOrderService materialOrderService) {
        this.materialOrderService = materialOrderService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            List<MaterialOrder> all = materialOrderService.getAllMaterialOrders();
            return ResponseEntity.ok(all.stream().map(this::toTO).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_MATERIAL_ORDERS");
        }
    }

    @GetMapping("/stale-monitoring")
    public ResponseEntity<?> getStaleMonitoring() {
        try {
            List<MaterialOrder> list = materialOrderService.findStaleForMonitoring();
            return ResponseEntity.ok(list.stream().map(this::toTO).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_STALE_MATERIAL_ORDERS");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody MaterialOrderTO to) {
        try {
            MaterialOrder saved = materialOrderService.addMaterialOrder(toEntity(to));
            return ResponseEntity.ok(toTO(saved));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/transition-status")
    public ResponseEntity<?> transitionStatus(
            @PathVariable Long id,
            @RequestBody MaterialOrderStatusTransitionTO body) {
        try {
            EMaterialOrderStatus next = body != null ? body.getStatus() : null;
            MaterialOrder saved = materialOrderService.transitionStatus(id, next);
            return ResponseEntity.ok(toTO(saved));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    private MaterialOrderTO toTO(MaterialOrder e) {
        MaterialOrderTO to = new MaterialOrderTO();
        to.setId(e.getId());
        to.setQuantity(e.getQuantity());
        if (e.getMaterial() != null) {
            to.setMaterialId(e.getMaterial().getId());
            to.setMaterialName(e.getMaterial().getName());
            to.setMaterialCode(e.getMaterial().getCode());
        }
        if (e.getMaterialProvider() != null) {
            to.setMaterialProviderId(e.getMaterialProvider().getId());
            to.setMaterialProviderName(e.getMaterialProvider().getName());
        }
        to.setStatus(e.getStatus());
        to.setLastChanged(e.getLastChanged());
        to.setCertificateBase64(null);
        to.setCertificatePresent(e.getCertificate() != null && e.getCertificate().length > 0);
        return to;
    }

    private MaterialOrder toEntity(MaterialOrderTO to) {
        MaterialOrder e = new MaterialOrder();
        if (to.getId() != null) {
            e.setId(to.getId());
        }
        e.setQuantity(to.getQuantity() == null ? 0 : to.getQuantity());
        Material m = new Material();
        m.setId(to.getMaterialId());
        e.setMaterial(m);
        MaterialProvider p = new MaterialProvider();
        p.setId(to.getMaterialProviderId());
        e.setMaterialProvider(p);
        e.setStatus(null);
        e.setCertificate(null);
        return e;
    }
}

