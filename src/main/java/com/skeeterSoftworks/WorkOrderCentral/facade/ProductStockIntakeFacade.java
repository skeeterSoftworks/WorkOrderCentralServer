package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.service.ProductStockIntakeService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductStockIntakeTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/stock/product-intakes")
@CrossOrigin(origins = "*")
public class ProductStockIntakeFacade {

    private final ProductStockIntakeService productStockIntakeService;

    public ProductStockIntakeFacade(ProductStockIntakeService productStockIntakeService) {
        this.productStockIntakeService = productStockIntakeService;
    }

    @GetMapping("/recent")
    public ResponseEntity<?> listRecent(@RequestParam(defaultValue = "50") int limit) {
        try {
            return ResponseEntity.ok(productStockIntakeService.listRecent(limit));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_PRODUCT_STOCK_INTAKES");
        }
    }

    @GetMapping("/work-orders")
    public ResponseEntity<?> listWorkOrders(@RequestParam long productId) {
        try {
            if (productId <= 0) {
                return ResponseEntity.badRequest().body("PRODUCT_STOCK_INTAKE_PRODUCT_REQUIRED");
            }
            return ResponseEntity.ok(productStockIntakeService.listWorkOrderOptions(productId));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_PRODUCT_STOCK_INTAKE_WORK_ORDERS");
        }
    }

    @PostMapping("/record")
    public ResponseEntity<?> record(@RequestBody ProductStockIntakeTO body) {
        try {
            ProductStockIntakeTO saved = productStockIntakeService.recordIntake(body);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String msg = e.getMessage();
            if ("PRODUCT_STOCK_INTAKE_PRODUCT_REQUIRED".equals(msg)
                    || "PRODUCT_STOCK_INTAKE_INVALID_QUANTITY".equals(msg)
                    || "PRODUCT_STOCK_INTAKE_WORK_ORDER_REQUIRED".equals(msg)
                    || "PRODUCT_STOCK_INTAKE_PRODUCT_WORK_ORDER_MISMATCH".equals(msg)) {
                return ResponseEntity.badRequest().body(msg);
            }
            if ("PRODUCT_NOT_FOUND".equals(msg) || "PRODUCT_STOCK_INTAKE_WORK_ORDER_NOT_FOUND".equals(msg)) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().body("ERROR_RECORDING_PRODUCT_STOCK_INTAKE");
        }
    }
}
