package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductOrder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductOrderRepository extends CrudRepository<ProductOrder, Long> {

    @Query("SELECT pol.purchaseOrder.id FROM ProductOrder pol WHERE pol.id = :productOrderLineId")
    Optional<Long> findPurchaseOrderIdByProductOrderLineId(@Param("productOrderLineId") Long productOrderLineId);
}
