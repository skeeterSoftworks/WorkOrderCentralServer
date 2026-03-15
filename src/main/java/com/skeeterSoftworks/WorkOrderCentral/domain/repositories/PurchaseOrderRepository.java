package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.PurchaseOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PurchaseOrderRepository extends CrudRepository<PurchaseOrder, Long> {

    @Override
    @EntityGraph(attributePaths = { "customer", "productOrderList", "productOrderList.product" })
    List<PurchaseOrder> findAll();
}

