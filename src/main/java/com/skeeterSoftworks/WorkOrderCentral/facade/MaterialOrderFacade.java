package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import com.skeeterSoftworks.WorkOrderCentral.service.MaterialOrderService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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

    private MaterialOrderTO toTO(MaterialOrder e) {
        return new MaterialOrderTO(
                e.getId(),
                e.getQuantity(),
                e.getMaterial() != null ? e.getMaterial().getId() : null,
                e.getMaterial() != null ? e.getMaterial().getName() : null,
                e.getMaterial() != null ? e.getMaterial().getCode() : null,
                e.getMaterialProvider() != null ? e.getMaterialProvider().getId() : null,
                e.getMaterialProvider() != null ? e.getMaterialProvider().getName() : null,
                e.getStatus(),
                null,
                e.getCertificate() != null && e.getCertificate().length > 0
        );
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

