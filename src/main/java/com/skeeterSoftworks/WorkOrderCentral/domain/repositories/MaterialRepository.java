package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialRepository extends CrudRepository<Material, Long> {
    @Override
    @EntityGraph(attributePaths = {"providers"})
    List<Material> findAll();

    @Override
    @EntityGraph(attributePaths = {"providers"})
    Optional<Material> findById(Long id);
}
