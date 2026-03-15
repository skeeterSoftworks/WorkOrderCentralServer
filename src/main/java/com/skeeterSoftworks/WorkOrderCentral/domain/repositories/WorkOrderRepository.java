package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WorkOrderRepository extends CrudRepository<WorkOrder, Long> {

    List<WorkOrder> findAll();

    boolean existsByPurchaseOrder_Id(Long purchaseOrderId);
}
