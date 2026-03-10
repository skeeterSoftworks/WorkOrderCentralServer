package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.PurchaseOrder;
import com.skeeterSoftworks.WorkOrderCentral.mapper.PurchaseOrderMapperService;
import com.skeeterSoftworks.WorkOrderCentral.service.PurchaseOrderService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.PurchaseOrderTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/purchaseorder")
public class PurchaseOrderFacade {

    private final PurchaseOrderService purchaseOrderService;
    private final PurchaseOrderMapperService purchaseOrderMapperService;

    @Autowired
    public PurchaseOrderFacade(PurchaseOrderService purchaseOrderService, PurchaseOrderMapperService purchaseOrderMapperService) {
        this.purchaseOrderService = purchaseOrderService;
        this.purchaseOrderMapperService = purchaseOrderMapperService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            List<PurchaseOrder> all = purchaseOrderService.getAllPurchaseOrders();
            return ResponseEntity.ok(all.stream().map(purchaseOrderMapperService::mapToTO).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_PURCHASE_ORDERS");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return purchaseOrderService.getPurchaseOrderById(id)
                    .map(purchaseOrderMapperService::mapToTO)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_PURCHASE_ORDER");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody PurchaseOrderTO purchaseOrderTO) {
        log.debug("Facade call: addPurchaseOrder");

        try {
            PurchaseOrder entity = purchaseOrderMapperService.mapToEntity(purchaseOrderTO);
            PurchaseOrder saved = purchaseOrderService.savePurchaseOrder(entity);
            return ResponseEntity.ok(purchaseOrderMapperService.mapToTO(saved));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_SAVING_PURCHASE_ORDER");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody PurchaseOrderTO purchaseOrderTO) {
        log.debug("Facade call: updatePurchaseOrder");

        if (purchaseOrderTO.getId() == null || purchaseOrderTO.getId() <= 0) {
            log.error("Invalid id for update: {}", purchaseOrderTO.getId());
            return ResponseEntity.badRequest().body("INVALID_ID");
        }

        try {
            PurchaseOrder entity = purchaseOrderMapperService.mapToEntity(purchaseOrderTO);
            PurchaseOrder updated = purchaseOrderService.updatePurchaseOrder(entity);
            return ResponseEntity.ok(purchaseOrderMapperService.mapToTO(updated));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            purchaseOrderService.deletePurchaseOrder(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
