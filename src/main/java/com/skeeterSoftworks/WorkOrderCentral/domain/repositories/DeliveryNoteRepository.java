package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.DeliveryNote;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface DeliveryNoteRepository extends CrudRepository<DeliveryNote, Long> {

    List<DeliveryNote> findByMaterialOrderLine_IdOrderByReceivedAtDescIdDesc(Long materialOrderLineId);

    List<DeliveryNote> findByMaterialOrder_IdOrderByReceivedAtDescIdDesc(Long materialOrderId);

    boolean existsByMaterialOrder_Id(Long materialOrderId);

    @Query("""
            SELECT COALESCE(SUM(d.quantity), 0) FROM DeliveryNote d
            WHERE d.materialOrderLine.id = :lineId
            """)
    int sumQuantityByMaterialOrderLineId(@Param("lineId") Long lineId);

    @Query("""
            SELECT l.id FROM MaterialOrderLine l
            WHERE l.materialOrder.id = :orderId
              AND (
                  SELECT COALESCE(SUM(d.quantity), 0) FROM DeliveryNote d
                  WHERE d.materialOrderLine.id = l.id
              ) >= l.quantity
            """)
    Set<Long> findFullyReceivedLineIdsByMaterialOrderId(@Param("orderId") Long orderId);
}
