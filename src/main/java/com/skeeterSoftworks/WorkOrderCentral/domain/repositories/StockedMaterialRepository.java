package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockedMaterial;
import org.springframework.data.repository.CrudRepository;

public interface StockedMaterialRepository extends CrudRepository<StockedMaterial, Long> {
}
