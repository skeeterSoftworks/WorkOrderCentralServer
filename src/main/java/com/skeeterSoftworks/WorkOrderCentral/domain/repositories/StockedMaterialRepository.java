package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockedMaterial;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StockedMaterialRepository extends CrudRepository<StockedMaterial, Long> {

    Optional<StockedMaterial> findByStockLocation_IdAndMaterial_Id(Long stockLocationId, Long materialId);

    @Query("SELECT COALESCE(SUM(sm.quantity), 0) FROM StockedMaterial sm WHERE sm.material.id = :materialId")
    long sumQuantityByMaterialId(@Param("materialId") Long materialId);
}
