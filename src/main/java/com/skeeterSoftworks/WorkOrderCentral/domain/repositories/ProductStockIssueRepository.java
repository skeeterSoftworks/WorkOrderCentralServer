package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductStockIssue;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProductStockIssueRepository extends CrudRepository<ProductStockIssue, Long> {

    @EntityGraph(attributePaths = {"product", "workOrder", "workOrder.productOrder", "workOrder.productOrder.purchaseOrder", "workOrder.productOrder.purchaseOrder.customer"})
    @Query("""
            SELECT i FROM ProductStockIssue i
            WHERE i.workOrder.productOrder.id IN :productOrderIds
            """)
    List<ProductStockIssue> findByProductOrderIds(@Param("productOrderIds") Collection<Long> productOrderIds);

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM ProductStockIssue i WHERE i.workOrder.id = :workOrderId")
    long sumQuantityByWorkOrderId(@Param("workOrderId") Long workOrderId);

    void deleteByWorkOrder_Id(Long workOrderId);
}
