package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.service.StockService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductAvailableStockTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/stock")
@CrossOrigin(origins = "*")
public class StockFacade {

    private final StockService stockService;

    @Autowired
    public StockFacade(StockService stockService) {
        this.stockService = stockService;
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
}
