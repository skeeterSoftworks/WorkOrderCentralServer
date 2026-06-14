package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderLine;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MaterialOrderLineRepository extends CrudRepository<MaterialOrderLine, Long> {

    List<MaterialOrderLine> findByMaterialOrder_Id(Long materialOrderId);

    boolean existsByMaterialOrder_Id(Long materialOrderId);
}
