package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.service.StockProductInventoryService;
import com.skeeterSoftworks.WorkOrderCentral.service.StockService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductAvailableStockTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductStockAvailabilityTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/stock")
@CrossOrigin(origins = "*")
public class StockFacade {

    private final StockService stockService;
    private final StockProductInventoryService stockProductInventoryService;

    @Autowired
    public StockFacade(StockService stockService, StockProductInventoryService stockProductInventoryService) {
        this.stockService = stockService;
        this.stockProductInventoryService = stockProductInventoryService;
    }

    @GetMapping("/products-availability")
    public ResponseEntity<?> getProductsAvailability() {
        try {
            List<ProductAvailableStockTO> list = stockService.getAvailableFinishedGoodStockByProduct();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_PRODUCT_STOCK");
        }
    }

    @GetMapping("/products/{productId}/availability")
    public ResponseEntity<?> getProductStockAvailability(@PathVariable Long productId) {
        try {
            if (productId == null || productId <= 0) {
                return ResponseEntity.badRequest().body("INVALID_PRODUCT_ID");
            }
            ProductStockAvailabilityTO row = stockProductInventoryService.getAvailableProductStock(productId);
            if (row == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(row);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_PRODUCT_STOCK_AVAILABILITY");
        }
    }
}
