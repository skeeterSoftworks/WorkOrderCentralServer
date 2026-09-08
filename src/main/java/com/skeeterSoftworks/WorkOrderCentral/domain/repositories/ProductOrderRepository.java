package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long>, JpaSpecificationExecutor<ProductOrder> {

    long countByProduct_Id(Long productId);

    long countByPurchaseOrder_Id(Long purchaseOrderId);

    @Query("SELECT pol.purchaseOrder.id FROM ProductOrder pol WHERE pol.id = :productOrderLineId")
    Optional<Long> findPurchaseOrderIdByProductOrderLineId(@Param("productOrderLineId") Long productOrderLineId);

    @Override
    @EntityGraph(attributePaths = {"purchaseOrder", "purchaseOrder.customer", "product"})
    Page<ProductOrder> findAll(Specification<ProductOrder> spec, Pageable pageable);
}
