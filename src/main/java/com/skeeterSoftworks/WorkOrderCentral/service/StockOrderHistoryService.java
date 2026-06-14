package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockAssignmentOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.StockAssignmentOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.mapper.StockOrderHistoryMapperService;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EStockOrderHistoryProductType;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.StockOrderHistoryPageTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.StockOrderHistoryRowTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class StockOrderHistoryService {

    private final StockAssignmentOrderRepository stockAssignmentOrderRepository;
    private final StockOrderHistoryMapperService stockOrderHistoryMapperService;

    public StockOrderHistoryService(
            StockAssignmentOrderRepository stockAssignmentOrderRepository,
            StockOrderHistoryMapperService stockOrderHistoryMapperService) {
        this.stockAssignmentOrderRepository = stockAssignmentOrderRepository;
        this.stockOrderHistoryMapperService = stockOrderHistoryMapperService;
    }

    @Transactional(readOnly = true)
    public StockOrderHistoryPageTO search(
            StockOrderHistorySearchCriteria criteria,
            int page,
            int size,
            String sortBy,
            boolean asc) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 200);

        EStockOrderHistoryProductType productType = criteria != null ? criteria.getProductType() : null;
        if (productType == EStockOrderHistoryProductType.MATERIAL) {
            return emptyPage(safePage, safeSize);
        }

        StockOrderHistorySearchCriteria effectiveCriteria = criteria != null
                ? criteria
                : StockOrderHistorySearchCriteria.builder().build();
        Specification<StockAssignmentOrder> spec =
                StockAssignmentOrderSearchSpecifications.assignedHistoryFrom(effectiveCriteria);
        Pageable pageable = PageRequest.of(safePage, safeSize, buildSort(sortBy, asc));
        Page<StockAssignmentOrder> result = stockAssignmentOrderRepository.findAll(spec, pageable);
        List<StockOrderHistoryRowTO> content = result.getContent().stream()
                .map(stockOrderHistoryMapperService::mapFinishedProductAssignment)
                .toList();
        return new StockOrderHistoryPageTO(content, result.getTotalElements(), result.getNumber(), result.getSize());
    }

    private static StockOrderHistoryPageTO emptyPage(int page, int size) {
        return new StockOrderHistoryPageTO(List.of(), 0, page, size);
    }

    private static Sort buildSort(String sortBy, boolean asc) {
        Sort.Direction direction = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        String field = StringUtils.hasText(sortBy) ? sortBy.trim() : "assignedAt";
        return switch (field) {
            case "code", "quantity", "assignedAt", "assignedByFullName" ->
                    Sort.by(new Sort.Order(direction, field));
            case "productName", "productReference" -> Sort.by(new Sort.Order(direction, "product.name"));
            case "workOrderId" -> Sort.by(new Sort.Order(direction, "workOrder.id"));
            default -> Sort.by(new Sort.Order(direction, "assignedAt"));
        };
    }
}
