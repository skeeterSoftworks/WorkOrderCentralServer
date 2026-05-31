package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMaterialOrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MaterialOrderRepository extends CrudRepository<MaterialOrder, Long> {

    @Override
    @EntityGraph(attributePaths = {"material", "material.providers", "materialProvider"})
    List<MaterialOrder> findAll();

    @Override
    @EntityGraph(attributePaths = {"material", "material.providers", "materialProvider"})
    Optional<MaterialOrder> findById(Long id);

    @EntityGraph(attributePaths = {"material", "materialProvider"})
    @Query("""
            SELECT m FROM MaterialOrder m
            WHERE (m.lastChanged IS NULL OR m.lastChanged < :threshold)
              AND m.status NOT IN :excluded
            """)
    List<MaterialOrder> findStaleMonitoringCandidates(
            @Param("threshold") LocalDateTime threshold,
            @Param("excluded") Collection<EMaterialOrderStatus> excluded);

    @EntityGraph(attributePaths = {"material", "materialProvider"})
    List<MaterialOrder> findByStatus(EMaterialOrderStatus status);

    boolean existsByCode(String code);
}

