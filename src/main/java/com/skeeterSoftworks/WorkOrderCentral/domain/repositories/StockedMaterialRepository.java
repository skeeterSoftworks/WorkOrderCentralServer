package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockedMaterial;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface StockedMaterialRepository extends CrudRepository<StockedMaterial, Long> {

    Optional<StockedMaterial> findByStockLocation_IdAndMaterial_Id(Long stockLocationId, Long materialId);
}
