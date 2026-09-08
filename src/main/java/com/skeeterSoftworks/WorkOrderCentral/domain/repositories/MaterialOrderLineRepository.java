package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MaterialOrderLineRepository extends JpaRepository<MaterialOrderLine, Long>, JpaSpecificationExecutor<MaterialOrderLine> {

    List<MaterialOrderLine> findByMaterialOrder_Id(Long materialOrderId);

    boolean existsByMaterialOrder_Id(Long materialOrderId);

    @Override
    @EntityGraph(attributePaths = {"materialOrder", "materialOrder.materialProvider", "material"})
    Page<MaterialOrderLine> findAll(Specification<MaterialOrderLine> spec, Pageable pageable);
}
