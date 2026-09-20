package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkSessionToolUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorkSessionToolUsageRepository extends
        JpaRepository<WorkSessionToolUsage, Long>,
        JpaSpecificationExecutor<WorkSessionToolUsage> {

    @Override
    @EntityGraph(attributePaths = {
            "technologySnapshot",
            "technologySnapshot.workSession",
            "technologySnapshot.workSession.faultyProducts",
            "technologySnapshot.workSession.workOrder",
            "technologySnapshot.workSession.workOrder.productOrder",
            "technologySnapshot.workSession.workOrder.productOrder.product"
    })
    Page<WorkSessionToolUsage> findAll(Specification<WorkSessionToolUsage> spec, Pageable pageable);
}
