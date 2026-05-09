package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialOrderRepository extends CrudRepository<MaterialOrder, Long> {

    @Override
    @EntityGraph(attributePaths = {"material", "material.providers", "materialProvider"})
    List<MaterialOrder> findAll();

    @Override
    @EntityGraph(attributePaths = {"material", "material.providers", "materialProvider"})
    Optional<MaterialOrder> findById(Long id);
}

