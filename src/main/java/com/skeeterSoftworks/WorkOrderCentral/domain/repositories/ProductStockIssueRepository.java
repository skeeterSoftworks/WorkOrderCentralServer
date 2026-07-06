package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductStockIssue;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ProductStockIssueRepository extends CrudRepository<ProductStockIssue, Long> {

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM ProductStockIssue i WHERE i.workOrder.id = :workOrderId")
    long sumQuantityByWorkOrderId(@Param("workOrderId") Long workOrderId);
}
