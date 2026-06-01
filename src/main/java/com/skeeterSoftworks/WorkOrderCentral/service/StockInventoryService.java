package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockLocation;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockedMaterial;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.StockLocationRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.StockedMaterialRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialReceptionStockAllocationTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockInventoryService {

    private final StockLocationRepository stockLocationRepository;
    private final StockedMaterialRepository stockedMaterialRepository;

    public StockInventoryService(
            StockLocationRepository stockLocationRepository,
            StockedMaterialRepository stockedMaterialRepository) {
        this.stockLocationRepository = stockLocationRepository;
        this.stockedMaterialRepository = stockedMaterialRepository;
    }

    /**
     * Adds material units to stock locations. Allocations with the same location id are merged.
     */
    @Transactional
    public void applyReceptionStockAllocations(Material material, int expectedTotal, List<MaterialReceptionStockAllocationTO> allocations)
            throws Exception {
        if (material == null || material.getId() == null) {
            throw new Exception("MATERIAL_NOT_FOUND");
        }
        if (allocations == null || allocations.isEmpty()) {
            throw new Exception("MATERIAL_ORDER_RECEPTION_STOCK_ALLOCATION_REQUIRED");
        }

        Map<Long, Integer> quantityByLocation = new HashMap<>();
        for (MaterialReceptionStockAllocationTO line : allocations) {
            if (line == null || line.getStockLocationId() == null || line.getStockLocationId() <= 0) {
                throw new Exception("MATERIAL_ORDER_RECEPTION_STOCK_LOCATION_REQUIRED");
            }
            if (line.getQuantity() == null || line.getQuantity() <= 0) {
                throw new Exception("MATERIAL_ORDER_RECEPTION_STOCK_QUANTITY_INVALID");
            }
            quantityByLocation.merge(line.getStockLocationId(), line.getQuantity(), Integer::sum);
        }

        int allocatedTotal = quantityByLocation.values().stream().mapToInt(Integer::intValue).sum();
        if (allocatedTotal != expectedTotal) {
            throw new Exception("MATERIAL_ORDER_RECEPTION_STOCK_ALLOCATION_MISMATCH");
        }

        Long materialId = material.getId();
        for (Map.Entry<Long, Integer> entry : quantityByLocation.entrySet()) {
            Long locationId = entry.getKey();
            int qty = entry.getValue();
            StockLocation location = stockLocationRepository.findById(locationId)
                    .orElseThrow(() -> new Exception("STOCK_LOCATION_NOT_FOUND"));

            StockedMaterial stocked = stockedMaterialRepository
                    .findByStockLocation_IdAndMaterial_Id(locationId, materialId)
                    .orElseGet(() -> {
                        StockedMaterial sm = new StockedMaterial();
                        sm.setStockLocation(location);
                        sm.setMaterial(material);
                        sm.setQuantity(0);
                        return sm;
                    });
            stocked.setQuantity(stocked.getQuantity() + qty);
            stockedMaterialRepository.save(stocked);
        }
    }
}
