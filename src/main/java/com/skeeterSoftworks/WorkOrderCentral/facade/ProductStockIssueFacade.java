package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.service.ProductStockIssueService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductStockIssueRequestTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/stock/product-issues")
@CrossOrigin(origins = "*")
public class ProductStockIssueFacade {

    private final ProductStockIssueService productStockIssueService;

    public ProductStockIssueFacade(ProductStockIssueService productStockIssueService) {
        this.productStockIssueService = productStockIssueService;
    }

    @GetMapping("/work-orders")
    public ResponseEntity<?> listEligibleWorkOrders() {
        try {
            return ResponseEntity.ok(productStockIssueService.listEligibleWorkOrders());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_PRODUCT_STOCK_ISSUE_WORK_ORDERS");
        }
    }

    @PostMapping("/issue")
    public ResponseEntity<?> issue(@RequestBody ProductStockIssueRequestTO body) {
        try {
            return ResponseEntity.ok(productStockIssueService.issueFromStock(body));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String msg = e.getMessage();
            if ("PRODUCT_STOCK_ISSUE_WORK_ORDER_REQUIRED".equals(msg)
                    || "PRODUCT_STOCK_ISSUE_WORK_ORDER_NOT_COMPLETE".equals(msg)
                    || "PRODUCT_STOCK_ISSUE_ALREADY_FULFILLED".equals(msg)
                    || "PRODUCT_STOCK_ISSUE_EXCEEDS_REMAINING".equals(msg)
                    || "PRODUCT_STOCK_ISSUE_INSUFFICIENT_STOCK".equals(msg)
                    || "PRODUCT_STOCK_ISSUE_PRODUCT_REQUIRED".equals(msg)) {
                return ResponseEntity.badRequest().body(msg);
            }
            if ("PRODUCT_STOCK_ISSUE_WORK_ORDER_NOT_FOUND".equals(msg)) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().body("ERROR_ISSUING_PRODUCT_STOCK");
        }
    }
}
