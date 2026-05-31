package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockLocation;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.StockLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class StockLocationService {

    private final StockLocationRepository stockLocationRepository;

    public StockLocationService(StockLocationRepository stockLocationRepository) {
        this.stockLocationRepository = stockLocationRepository;
    }

    @Transactional(readOnly = true)
    public List<StockLocation> getAllStockLocations() {
        return stockLocationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<StockLocation> getStockLocationById(Long id) {
        return stockLocationRepository.findById(id);
    }

    @Transactional
    public StockLocation addStockLocation(String stockLocationCode) throws Exception {
        String code = normalizeCode(stockLocationCode);
        if (stockLocationRepository.existsByStockLocationCodeIgnoreCase(code)) {
            throw new Exception("STOCK_LOCATION_CODE_ALREADY_EXISTS");
        }
        StockLocation location = new StockLocation();
        location.setStockLocationCode(code);
        return stockLocationRepository.save(location);
    }

    @Transactional
    public StockLocation updateStockLocation(Long id, String stockLocationCode) throws Exception {
        StockLocation existing = stockLocationRepository.findById(id)
                .orElseThrow(() -> new Exception("STOCK_LOCATION_NOT_FOUND"));
        String code = normalizeCode(stockLocationCode);
        if (stockLocationRepository.existsByStockLocationCodeIgnoreCaseAndIdNot(code, id)) {
            throw new Exception("STOCK_LOCATION_CODE_ALREADY_EXISTS");
        }
        existing.setStockLocationCode(code);
        return stockLocationRepository.save(existing);
    }

    @Transactional
    public void deleteStockLocation(Long id) throws Exception {
        if (!stockLocationRepository.existsById(id)) {
            throw new Exception("STOCK_LOCATION_NOT_FOUND");
        }
        stockLocationRepository.deleteById(id);
    }

    private static String normalizeCode(String stockLocationCode) {
        if (!StringUtils.hasText(stockLocationCode)) {
            throw new IllegalArgumentException("STOCK_LOCATION_CODE_REQUIRED");
        }
        return stockLocationCode.trim();
    }
}
