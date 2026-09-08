package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderLine;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class MaterialOrderHistorySpecifications {

    private MaterialOrderHistorySpecifications() {
    }

    public static Specification<MaterialOrderLine> from(String materialCode, Long materialProviderId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Class<?> resultType = query.getResultType();
            boolean isCount = resultType == Long.class || resultType == long.class;
            boolean filterByProvider = materialProviderId != null && materialProviderId > 0;

            Join<MaterialOrderLine, MaterialOrder> order = null;
            if (!isCount || filterByProvider) {
                order = root.join("materialOrder", JoinType.INNER);
            }

            if (StringUtils.hasText(materialCode)) {
                Join<MaterialOrderLine, Material> material = root.join("material", JoinType.INNER);
                String pattern = "%" + materialCode.trim() + "%";
                predicates.add(cb.like(material.get("code"), pattern));
            }
            if (filterByProvider) {
                Join<MaterialOrder, MaterialProvider> provider = order.join("materialProvider", JoinType.LEFT);
                predicates.add(cb.equal(provider.get("id"), materialProviderId));
            }

            if (!isCount && order != null) {
                query.orderBy(cb.desc(order.get("createdAt")), cb.desc(root.get("id")));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
