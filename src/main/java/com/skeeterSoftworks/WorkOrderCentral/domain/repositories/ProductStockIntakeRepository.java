package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductStockIntake;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ProductStockIntakeRepository extends CrudRepository<ProductStockIntake, Long> {

    List<ProductStockIntake> findAllByOrderByReceivedAtDescIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"product", "workOrder"})
    List<ProductStockIntake> findAllByReceivedAtGreaterThanEqualAndReceivedAtLessThanOrderByReceivedAtDescIdDesc(
            LocalDateTime receivedAtStartInclusive,
            LocalDateTime receivedAtEndExclusive,
            Pageable pageable);

    @EntityGraph(attributePaths = {"product", "workOrder", "workOrder.productOrder", "workOrder.productOrder.purchaseOrder", "workOrder.productOrder.purchaseOrder.customer"})
    @Query("""
            SELECT i FROM ProductStockIntake i
            WHERE i.workOrder.productOrder.id IN :productOrderIds
            """)
    List<ProductStockIntake> findByProductOrderIds(@Param("productOrderIds") Collection<Long> productOrderIds);

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM ProductStockIntake i WHERE i.workOrder.id = :workOrderId")
    long sumQuantityByWorkOrderId(@Param("workOrderId") Long workOrderId);

    @Query("SELECT COALESCE(SUM(i.quantity - i.surplusQuantity), 0) FROM ProductStockIntake i WHERE i.workOrder.id = :workOrderId")
    long sumOrderQuantityByWorkOrderId(@Param("workOrderId") Long workOrderId);

    void deleteByWorkOrder_Id(Long workOrderId);
}
