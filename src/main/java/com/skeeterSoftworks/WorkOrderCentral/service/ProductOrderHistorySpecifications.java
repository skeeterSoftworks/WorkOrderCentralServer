package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Customer;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.PurchaseOrder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class ProductOrderHistorySpecifications {

    private ProductOrderHistorySpecifications() {
    }

    public static Specification<ProductOrder> from(String productReference, Long customerId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Class<?> resultType = query.getResultType();
            boolean isCount = resultType == Long.class || resultType == long.class;
            boolean filterByCustomer = customerId != null && customerId > 0;

            Join<ProductOrder, PurchaseOrder> purchaseOrder = null;
            if (!isCount || filterByCustomer) {
                purchaseOrder = root.join("purchaseOrder", JoinType.INNER);
            }

            if (StringUtils.hasText(productReference)) {
                Join<ProductOrder, Product> product = root.join("product", JoinType.LEFT);
                String pattern = "%" + productReference.trim() + "%";
                predicates.add(cb.like(product.get("reference"), pattern));
            }
            if (filterByCustomer) {
                Join<PurchaseOrder, Customer> customer = purchaseOrder.join("customer", JoinType.LEFT);
                predicates.add(cb.equal(customer.get("id"), customerId));
            }

            if (!isCount && purchaseOrder != null) {
                query.orderBy(cb.desc(purchaseOrder.get("createdAt")), cb.desc(root.get("id")));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
