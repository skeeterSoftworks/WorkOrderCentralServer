package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EWorkOrderState;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WorkOrderRepository extends CrudRepository<WorkOrder, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "productOrder",
            "productOrder.product",
            "productOrder.purchaseOrder",
            "productOrder.purchaseOrder.customer"
    })
    List<WorkOrder> findAll();

    @Override
    @EntityGraph(attributePaths = {
            "productOrder",
            "productOrder.product",
            "productOrder.purchaseOrder",
            "productOrder.purchaseOrder.customer"
    })
    Optional<WorkOrder> findById(Long id);

    boolean existsByProductOrder_Id(Long productOrderId);

    Optional<WorkOrder> findByProductOrder_Id(Long productOrderId);

    boolean existsByProductOrder_PurchaseOrder_Id(Long purchaseOrderId);

    @EntityGraph(attributePaths = {
            "productOrder",
            "productOrder.product",
            "productOrder.purchaseOrder",
            "productOrder.purchaseOrder.customer"
    })
    List<WorkOrder> findAllByIdIn(Collection<Long> ids);

    @EntityGraph(attributePaths = {
            "productOrder",
            "productOrder.product",
            "productOrder.purchaseOrder"
    })
    List<WorkOrder> findByProductOrder_Product_IdOrderByIdDesc(Long productId);

    long countByProductOrder_PurchaseOrder_Id(Long purchaseOrderId);

    long countByProductOrder_PurchaseOrder_IdAndState(Long purchaseOrderId, EWorkOrderState state);

    @EntityGraph(attributePaths = {
            "productOrder",
            "productOrder.product",
            "productOrder.purchaseOrder",
            "productOrder.purchaseOrder.customer"
    })
    List<WorkOrder> findByStateOrderByIdDesc(EWorkOrderState state);
}
