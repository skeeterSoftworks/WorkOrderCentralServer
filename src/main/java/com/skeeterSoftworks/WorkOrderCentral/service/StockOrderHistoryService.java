package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialAssignmentOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockAssignmentOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialAssignmentOrderRepository;
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

import java.util.ArrayList;
import java.util.List;

@Service
public class StockOrderHistoryService {

    private final StockAssignmentOrderRepository stockAssignmentOrderRepository;
    private final MaterialAssignmentOrderRepository materialAssignmentOrderRepository;
    private final StockOrderHistoryMapperService stockOrderHistoryMapperService;

    public StockOrderHistoryService(
            StockAssignmentOrderRepository stockAssignmentOrderRepository,
            MaterialAssignmentOrderRepository materialAssignmentOrderRepository,
            StockOrderHistoryMapperService stockOrderHistoryMapperService) {
        this.stockAssignmentOrderRepository = stockAssignmentOrderRepository;
        this.materialAssignmentOrderRepository = materialAssignmentOrderRepository;
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
        StockOrderHistorySearchCriteria effectiveCriteria = criteria != null
                ? criteria
                : StockOrderHistorySearchCriteria.builder().build();

        if (productType == EStockOrderHistoryProductType.MATERIAL) {
            Specification<MaterialAssignmentOrder> spec =
                    MaterialAssignmentOrderSearchSpecifications.assignedHistoryFrom(effectiveCriteria);
            Pageable pageable = PageRequest.of(safePage, safeSize, buildMaterialSort(sortBy, asc));
            Page<MaterialAssignmentOrder> result = materialAssignmentOrderRepository.findAll(spec, pageable);
            List<StockOrderHistoryRowTO> content = result.getContent().stream()
                    .map(stockOrderHistoryMapperService::mapMaterialAssignment)
                    .toList();
            return new StockOrderHistoryPageTO(content, result.getTotalElements(), result.getNumber(), result.getSize());
        }

        if (productType == EStockOrderHistoryProductType.FINISHED_PRODUCT) {
            Specification<StockAssignmentOrder> spec =
                    StockAssignmentOrderSearchSpecifications.assignedHistoryFrom(effectiveCriteria);
            Pageable pageable = PageRequest.of(safePage, safeSize, buildProductSort(sortBy, asc));
            Page<StockAssignmentOrder> result = stockAssignmentOrderRepository.findAll(spec, pageable);
            List<StockOrderHistoryRowTO> content = result.getContent().stream()
                    .map(stockOrderHistoryMapperService::mapFinishedProductAssignment)
                    .toList();
            return new StockOrderHistoryPageTO(content, result.getTotalElements(), result.getNumber(), result.getSize());
        }

        List<StockOrderHistoryRowTO> combined = new ArrayList<>();
        long total = 0;
        Specification<StockAssignmentOrder> productSpec =
                StockAssignmentOrderSearchSpecifications.assignedHistoryFrom(effectiveCriteria);
        Page<StockAssignmentOrder> productPage = stockAssignmentOrderRepository.findAll(
                productSpec,
                PageRequest.of(0, Integer.MAX_VALUE, buildProductSort(sortBy, asc)));
        combined.addAll(productPage.getContent().stream()
                .map(stockOrderHistoryMapperService::mapFinishedProductAssignment)
                .toList());

        Specification<MaterialAssignmentOrder> materialSpec =
                MaterialAssignmentOrderSearchSpecifications.assignedHistoryFrom(effectiveCriteria);
        Page<MaterialAssignmentOrder> materialPage = materialAssignmentOrderRepository.findAll(
                materialSpec,
                PageRequest.of(0, Integer.MAX_VALUE, buildMaterialSort(sortBy, asc)));
        combined.addAll(materialPage.getContent().stream()
                .map(stockOrderHistoryMapperService::mapMaterialAssignment)
                .toList());

        combined.sort((a, b) -> compareHistoryRows(a, b, sortBy, asc));
        total = combined.size();
        int from = Math.min(safePage * safeSize, combined.size());
        int to = Math.min(from + safeSize, combined.size());
        List<StockOrderHistoryRowTO> pageContent = combined.subList(from, to);
        return new StockOrderHistoryPageTO(pageContent, total, safePage, safeSize);
    }

    private static int compareHistoryRows(
            StockOrderHistoryRowTO a,
            StockOrderHistoryRowTO b,
            String sortBy,
            boolean asc) {
        int cmp = switch (StringUtils.hasText(sortBy) ? sortBy.trim() : "assignedAt") {
            case "code" -> nullSafeCompare(a.getCode(), b.getCode());
            case "quantity" -> Integer.compare(
                    a.getQuantity() != null ? a.getQuantity() : 0,
                    b.getQuantity() != null ? b.getQuantity() : 0);
            case "workOrderId" -> Long.compare(
                    a.getWorkOrderId() != null ? a.getWorkOrderId() : 0L,
                    b.getWorkOrderId() != null ? b.getWorkOrderId() : 0L);
            case "assignedByFullName" -> nullSafeCompare(a.getAssignedByFullName(), b.getAssignedByFullName());
            case "productName" -> nullSafeCompare(a.getProductName(), b.getProductName());
            default -> nullSafeCompare(
                    a.getAssignedAt() != null ? a.getAssignedAt().toString() : "",
                    b.getAssignedAt() != null ? b.getAssignedAt().toString() : "");
        };
        return asc ? cmp : -cmp;
    }

    private static int nullSafeCompare(String a, String b) {
        return (a != null ? a : "").compareToIgnoreCase(b != null ? b : "");
    }

    private static Sort buildProductSort(String sortBy, boolean asc) {
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

    private static Sort buildMaterialSort(String sortBy, boolean asc) {
        Sort.Direction direction = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        String field = StringUtils.hasText(sortBy) ? sortBy.trim() : "assignedAt";
        return switch (field) {
            case "code", "quantity", "assignedAt", "assignedByFullName" ->
                    Sort.by(new Sort.Order(direction, field));
            case "workOrderId" -> Sort.by(new Sort.Order(direction, "workOrder.id"));
            case "productName", "productReference" ->
                    Sort.by(new Sort.Order(direction, "workOrder.productOrder.product.name"));
            default -> Sort.by(new Sort.Order(direction, "assignedAt"));
        };
    }
}
