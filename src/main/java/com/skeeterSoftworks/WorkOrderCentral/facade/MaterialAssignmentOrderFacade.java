package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.service.MaterialAssignmentInventoryService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.FulfillStockAssignmentOrderRequestTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialAssignmentOrderTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/stock/material-assignment-orders")
@CrossOrigin(origins = "*")
public class MaterialAssignmentOrderFacade {

    private final MaterialAssignmentInventoryService materialAssignmentInventoryService;

    public MaterialAssignmentOrderFacade(MaterialAssignmentInventoryService materialAssignmentInventoryService) {
        this.materialAssignmentInventoryService = materialAssignmentInventoryService;
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> getByCode(@PathVariable String code) {
        try {
            MaterialAssignmentOrderTO row = materialAssignmentInventoryService.getAssignmentOrderByCode(code);
            return ResponseEntity.ok(row);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String msg = e.getMessage();
            if ("MATERIAL_ASSIGNMENT_ORDER_NOT_FOUND".equals(msg)
                    || "MATERIAL_ASSIGNMENT_ORDER_INVALID_CODE".equals(msg)) {
                return ResponseEntity.badRequest().body(msg);
            }
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_MATERIAL_ASSIGNMENT_ORDER");
        }
    }

    @PostMapping("/fulfill")
    public ResponseEntity<?> fulfill(@RequestBody FulfillStockAssignmentOrderRequestTO body) {
        try {
            if (body == null) {
                return ResponseEntity.badRequest().body("MATERIAL_ASSIGNMENT_ORDER_INVALID_CODE");
            }
            MaterialAssignmentOrderTO result = materialAssignmentInventoryService.fulfillAssignmentOrderByCode(
                    body.getCode(),
                    body.getOperatorUserQrCode());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String msg = e.getMessage();
            if ("MATERIAL_ASSIGNMENT_ORDER_NOT_FOUND".equals(msg)
                    || "MATERIAL_ASSIGNMENT_ORDER_INVALID_CODE".equals(msg)
                    || "MATERIAL_ASSIGNMENT_ORDER_ALREADY_ASSIGNED".equals(msg)
                    || "MATERIAL_ASSIGNMENT_ORDER_INSUFFICIENT_STOCK".equals(msg)
                    || "MATERIAL_ASSIGNMENT_ORDER_EMPTY".equals(msg)) {
                return ResponseEntity.badRequest().body(msg);
            }
            return ResponseEntity.internalServerError().body("ERROR_FULFILLING_MATERIAL_ASSIGNMENT_ORDER");
        }
    }
}
