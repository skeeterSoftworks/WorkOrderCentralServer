package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockLocation;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockedMaterial;
import com.skeeterSoftworks.WorkOrderCentral.service.StockLocationService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialProviderTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.StockLocationTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.StockedMaterialTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/stock-locations")
@CrossOrigin(origins = "*")
public class StockLocationFacade {

    private final StockLocationService stockLocationService;

    public StockLocationFacade(StockLocationService stockLocationService) {
        this.stockLocationService = stockLocationService;
    }

    @Transactional(readOnly = true)
    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            List<StockLocation> all = stockLocationService.getAllStockLocations();
            return ResponseEntity.ok(all.stream().map(this::toTO).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_STOCK_LOCATIONS");
        }
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return stockLocationService.getStockLocationById(id)
                    .map(this::toTO)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_STOCK_LOCATION");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody StockLocationTO stockLocationTO) {
        try {
            if (!StringUtils.hasText(stockLocationTO.getStockLocationCode())) {
                return ResponseEntity.badRequest().body("STOCK_LOCATION_CODE_REQUIRED");
            }
            StockLocation saved = stockLocationService.addStockLocation(stockLocationTO.getStockLocationCode());
            return ResponseEntity.ok(toTO(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody StockLocationTO stockLocationTO) {
        if (stockLocationTO.getId() == null || stockLocationTO.getId() <= 0) {
            return ResponseEntity.badRequest().body("INVALID_ID");
        }
        if (!StringUtils.hasText(stockLocationTO.getStockLocationCode())) {
            return ResponseEntity.badRequest().body("STOCK_LOCATION_CODE_REQUIRED");
        }
        try {
            StockLocation updated = stockLocationService.updateStockLocation(
                    stockLocationTO.getId(),
                    stockLocationTO.getStockLocationCode());
            return ResponseEntity.ok(toTO(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            stockLocationService.deleteStockLocation(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    private StockLocationTO toTO(StockLocation location) {
        List<StockedMaterialTO> stocked = location.getStockedMaterials() == null
                ? List.of()
                : location.getStockedMaterials().stream().map(this::toStockedMaterialTO).toList();
        return new StockLocationTO(location.getId(), location.getStockLocationCode(), stocked);
    }

    private StockedMaterialTO toStockedMaterialTO(StockedMaterial sm) {
        MaterialTO materialTO = sm.getMaterial() == null ? null : toMaterialTO(sm.getMaterial());
        return new StockedMaterialTO(sm.getId(), sm.getQuantity(), materialTO);
    }

    private MaterialTO toMaterialTO(Material m) {
        List<MaterialProviderTO> providers = m.getProviders() == null
                ? List.of()
                : m.getProviders().stream().map(this::toProviderTO).toList();
        return new MaterialTO(
                m.getId(),
                m.getName(),
                m.getCode(),
                m.getProductsPerUnit(),
                m.getDiameter(),
                m.getWeight(),
                m.getLength(),
                m.getWidth(),
                providers);
    }

    private MaterialProviderTO toProviderTO(MaterialProvider p) {
        return new MaterialProviderTO(
                p.getId(),
                p.getName(),
                p.getContactPerson(),
                p.getEmailAddress(),
                p.getPhoneNumber(),
                p.getGrade());
    }
}
