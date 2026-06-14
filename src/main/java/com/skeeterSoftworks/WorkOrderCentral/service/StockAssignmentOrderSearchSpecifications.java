package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockAssignmentOrder;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EStockAssignmentOrderStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class StockAssignmentOrderSearchSpecifications {

    private StockAssignmentOrderSearchSpecifications() {
    }

    public static Specification<StockAssignmentOrder> assignedHistoryFrom(StockOrderHistorySearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), EStockAssignmentOrderStatus.ASSIGNED));
            predicates.add(cb.isNotNull(root.get("assignedAt")));

            if (criteria.getAssignedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("assignedAt"),
                        criteria.getAssignedFrom().atStartOfDay()));
            }
            if (criteria.getAssignedTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("assignedAt"),
                        criteria.getAssignedTo().atTime(LocalTime.MAX)));
            }

            if (StringUtils.hasText(criteria.getAssignedBy())) {
                String pattern = likePattern(criteria.getAssignedBy());
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("assignedByFullName")), pattern),
                        cb.like(cb.lower(root.get("assignedByUserQr")), pattern)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String likePattern(String raw) {
        return "%" + raw.trim().toLowerCase() + "%";
    }
}
