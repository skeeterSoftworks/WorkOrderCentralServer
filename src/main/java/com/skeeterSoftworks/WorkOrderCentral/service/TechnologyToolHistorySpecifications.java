package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkSessionToolUsage;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TechnologyToolHistorySpecifications {

    private TechnologyToolHistorySpecifications() {
    }

    public static Specification<WorkSessionToolUsage> from(
            String productReference,
            String toolName,
            String workOrderCode) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<?, ?> snapshot = root.join("technologySnapshot", JoinType.INNER);
            Join<?, ?> session = snapshot.join("workSession", JoinType.INNER);
            Join<?, ?> workOrder = session.join("workOrder", JoinType.LEFT);
            Join<?, ?> productOrder = workOrder.join("productOrder", JoinType.LEFT);
            Join<?, ?> product = productOrder.join("product", JoinType.LEFT);

            if (StringUtils.hasText(productReference)) {
                String q = "%" + productReference.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.like(cb.lower(cb.coalesce(product.get("reference"), "")), q));
            }
            if (StringUtils.hasText(toolName)) {
                String q = "%" + toolName.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.like(cb.lower(cb.coalesce(root.get("toolName"), "")), q));
            }
            if (StringUtils.hasText(workOrderCode)) {
                String q = "%" + workOrderCode.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.like(cb.lower(cb.coalesce(workOrder.get("code"), "")), q));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
