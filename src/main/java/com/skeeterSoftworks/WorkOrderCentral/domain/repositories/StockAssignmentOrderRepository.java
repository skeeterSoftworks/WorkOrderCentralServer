package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockAssignmentOrder;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EStockAssignmentOrderStatus;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockAssignmentOrderRepository extends CrudRepository<StockAssignmentOrder, Long>,
        JpaSpecificationExecutor<StockAssignmentOrder> {

    Optional<StockAssignmentOrder> findByCode(String code);

    boolean existsByCode(String code);

    List<StockAssignmentOrder> findByWorkOrder_IdOrderByIdAsc(Long workOrderId);

    Optional<StockAssignmentOrder> findFirstByWorkOrder_IdOrderByIdDesc(Long workOrderId);

    @Query("SELECT COALESCE(SUM(o.quantity), 0) FROM StockAssignmentOrder o WHERE o.product.id = :productId")
    long sumReservedQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT COALESCE(SUM(o.quantity), 0) FROM StockAssignmentOrder o "
            + "WHERE o.product.id = :productId AND o.status = :status")
    long sumQuantityByProductIdAndStatus(
            @Param("productId") Long productId,
            @Param("status") EStockAssignmentOrderStatus status);
}
