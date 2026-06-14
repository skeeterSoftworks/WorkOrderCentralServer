package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.service.StockProductInventoryService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.FulfillStockAssignmentOrderRequestTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.StockAssignmentOrderTO;
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
@RequestMapping("/stock/assignment-orders")
@CrossOrigin(origins = "*")
public class StockAssignmentOrderFacade {

    private final StockProductInventoryService stockProductInventoryService;

    public StockAssignmentOrderFacade(StockProductInventoryService stockProductInventoryService) {
        this.stockProductInventoryService = stockProductInventoryService;
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> getByCode(@PathVariable String code) {
        try {
            StockAssignmentOrderTO row = stockProductInventoryService.getAssignmentOrderByCode(code);
            return ResponseEntity.ok(row);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String msg = e.getMessage();
            if ("STOCK_ASSIGNMENT_ORDER_NOT_FOUND".equals(msg)
                    || "STOCK_ASSIGNMENT_ORDER_INVALID_CODE".equals(msg)) {
                return ResponseEntity.badRequest().body(msg);
            }
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_STOCK_ASSIGNMENT_ORDER");
        }
    }

    @PostMapping("/fulfill")
    public ResponseEntity<?> fulfill(@RequestBody FulfillStockAssignmentOrderRequestTO body) {
        try {
            if (body == null) {
                return ResponseEntity.badRequest().body("STOCK_ASSIGNMENT_ORDER_INVALID_CODE");
            }
            StockAssignmentOrderTO result = stockProductInventoryService.fulfillAssignmentOrderByCode(
                    body.getCode(),
                    body.getOperatorUserQrCode());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String msg = e.getMessage();
            if ("STOCK_ASSIGNMENT_ORDER_NOT_FOUND".equals(msg)
                    || "STOCK_ASSIGNMENT_ORDER_INVALID_CODE".equals(msg)
                    || "STOCK_ASSIGNMENT_ORDER_ALREADY_ASSIGNED".equals(msg)
                    || "STOCK_ASSIGNMENT_ORDER_INSUFFICIENT_STOCK".equals(msg)) {
                return ResponseEntity.badRequest().body(msg);
            }
            return ResponseEntity.internalServerError().body("ERROR_FULFILLING_STOCK_ASSIGNMENT_ORDER");
        }
    }
}
