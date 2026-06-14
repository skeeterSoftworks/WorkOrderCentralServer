package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrderStockAssignment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkOrderStockAssignmentRepository extends CrudRepository<WorkOrderStockAssignment, Long> {

    List<WorkOrderStockAssignment> findByWorkOrder_IdOrderByIdAsc(Long workOrderId);

    @Query("SELECT COALESCE(SUM(a.quantity), 0) FROM WorkOrderStockAssignment a WHERE a.product.id = :productId")
    long sumAssignedQuantityByProductId(@Param("productId") Long productId);
}
