package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialAssignmentOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MaterialAssignmentOrderRepository extends
        JpaRepository<MaterialAssignmentOrder, Long>,
        JpaSpecificationExecutor<MaterialAssignmentOrder> {

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = {
            "workOrder",
            "workOrder.productOrder",
            "workOrder.productOrder.product",
            "lines",
            "lines.material"
    })
    Optional<MaterialAssignmentOrder> findByCode(String code);

    @EntityGraph(attributePaths = {
            "workOrder",
            "workOrder.productOrder",
            "workOrder.productOrder.product",
            "lines",
            "lines.material"
    })
    Optional<MaterialAssignmentOrder> findFirstByWorkOrder_IdOrderByIdDesc(Long workOrderId);
}
