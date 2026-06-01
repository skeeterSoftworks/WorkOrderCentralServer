package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderReception;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialOrderReceptionRepository extends CrudRepository<MaterialOrderReception, Long> {

    @Override
    @EntityGraph(attributePaths = {"materialOrder", "materialOrder.material", "materialOrder.materialProvider"})
    List<MaterialOrderReception> findAll();

    @Override
    @EntityGraph(attributePaths = {"materialOrder", "materialOrder.material", "materialOrder.materialProvider"})
    Optional<MaterialOrderReception> findById(Long id);

    @EntityGraph(attributePaths = {"materialOrder", "materialOrder.material", "materialOrder.materialProvider"})
    List<MaterialOrderReception> findByMaterialOrder_Id(Long materialOrderId);

    boolean existsByMaterialOrder_Id(Long materialOrderId);

    @EntityGraph(attributePaths = {"materialOrder", "materialOrder.material", "materialOrder.materialProvider"})
    Optional<MaterialOrderReception> findFirstByMaterialOrder_Id(Long materialOrderId);
}
