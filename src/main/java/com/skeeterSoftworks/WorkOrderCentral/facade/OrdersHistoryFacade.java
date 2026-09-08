package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.service.OrdersHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/orders-history")
@CrossOrigin(origins = "*")
public class OrdersHistoryFacade {

    private final OrdersHistoryService ordersHistoryService;

    public OrdersHistoryFacade(OrdersHistoryService ordersHistoryService) {
        this.ordersHistoryService = ordersHistoryService;
    }

    @GetMapping("/products")
    public ResponseEntity<?> searchProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String productReference,
            @RequestParam(required = false) Long customerId) {
        try {
            return ResponseEntity.ok(
                    ordersHistoryService.searchProductOrders(productReference, customerId, page, size));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_PRODUCT_ORDER_HISTORY");
        }
    }

    @GetMapping("/materials")
    public ResponseEntity<?> searchMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String materialCode,
            @RequestParam(required = false) Long materialProviderId) {
        try {
            return ResponseEntity.ok(
                    ordersHistoryService.searchMaterialOrders(materialCode, materialProviderId, page, size));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_MATERIAL_ORDER_HISTORY");
        }
    }
}
