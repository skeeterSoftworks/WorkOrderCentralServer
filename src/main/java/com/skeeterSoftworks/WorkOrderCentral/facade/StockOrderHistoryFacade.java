package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.service.StockOrderHistorySearchCriteria;
import com.skeeterSoftworks.WorkOrderCentral.service.StockOrderHistoryService;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EStockOrderHistoryProductType;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.StockOrderHistoryPageTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/stock/order-history")
@CrossOrigin(origins = "*")
public class StockOrderHistoryFacade {

    private final StockOrderHistoryService stockOrderHistoryService;

    public StockOrderHistoryFacade(StockOrderHistoryService stockOrderHistoryService) {
        this.stockOrderHistoryService = stockOrderHistoryService;
    }

    @Transactional(readOnly = true)
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "assignedAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean asc,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate assignedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate assignedTo,
            @RequestParam(required = false) String assignedBy) {
        try {
            EStockOrderHistoryProductType productTypeEnum = resolveProductType(productType);
            StockOrderHistorySearchCriteria criteria = StockOrderHistorySearchCriteria.builder()
                    .productType(productTypeEnum)
                    .assignedFrom(assignedFrom)
                    .assignedTo(assignedTo)
                    .assignedBy(assignedBy)
                    .build();
            StockOrderHistoryPageTO pageTo = stockOrderHistoryService.search(
                    criteria, page, size, sortBy, asc);
            return ResponseEntity.ok(pageTo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("STOCK_ORDER_HISTORY_INVALID_PRODUCT_TYPE");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_STOCK_ORDER_HISTORY");
        }
    }

    private static EStockOrderHistoryProductType resolveProductType(String raw) {
        if (!StringUtils.hasText(raw) || "ALL".equalsIgnoreCase(raw.trim())) {
            return null;
        }
        return EStockOrderHistoryProductType.valueOf(raw.trim());
    }
}
