package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderReception;
import com.skeeterSoftworks.WorkOrderCentral.service.MaterialOrderReceptionService;
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

    @PostMapping("/record")
    public ResponseEntity<?> record(@RequestBody MaterialOrderReceptionTO body) {
        try {
            MaterialOrderReception saved = materialOrderReceptionService.recordReception(body);
            return ResponseEntity.ok(toTO(saved));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private MaterialOrderReceptionTO toTO(MaterialOrderReception r) {
        MaterialOrderReceptionTO to = new MaterialOrderReceptionTO();
        to.setId(r.getId());
        to.setReceivedAt(r.getReceivedAt());
        to.setReceivedQuantity(r.getReceivedQuantity());
        MaterialOrder order = r.getMaterialOrder();
        if (order != null) {
            to.setMaterialOrderId(order.getId());
            if (order.getMaterial() != null) {
                to.setMaterialCode(order.getMaterial().getCode());
                to.setMaterialName(order.getMaterial().getName());
            }
            if (order.getMaterialProvider() != null) {
                to.setMaterialProviderName(order.getMaterialProvider().getName());
            }
        }
        return to;
    }
}
