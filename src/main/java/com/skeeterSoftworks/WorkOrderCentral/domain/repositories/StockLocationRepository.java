package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockLocation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface StockLocationRepository extends CrudRepository<StockLocation, Long> {

    /** stockedMaterials is the only bag; material is ManyToOne and safe to fetch in the same graph. */
    @Override
    @EntityGraph(attributePaths = { "stockedMaterials", "stockedMaterials.material" })
    List<StockLocation> findAll();

    @Override
    @EntityGraph(attributePaths = { "stockedMaterials", "stockedMaterials.material" })
    Optional<StockLocation> findById(Long id);

    boolean existsByStockLocationCodeIgnoreCase(String stockLocationCode);

    boolean existsByStockLocationCodeIgnoreCaseAndIdNot(String stockLocationCode, Long id);
}
