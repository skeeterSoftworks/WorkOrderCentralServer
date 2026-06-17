package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderReception;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MaterialOrderReceptionRepository extends CrudRepository<MaterialOrderReception, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "materialOrder",
            "materialOrder.lines",
            "materialOrder.lines.material",
            "materialOrder.materialProvider",
            "materialOrderLine",
            "materialOrderLine.material",
            "deliveryNote"
    })
    List<MaterialOrderReception> findAll();

    @Override
    @EntityGraph(attributePaths = {
            "materialOrder",
            "materialOrder.lines",
            "materialOrder.lines.material",
            "materialOrder.materialProvider",
            "materialOrderLine",
            "materialOrderLine.material",
            "deliveryNote"
    })
    Optional<MaterialOrderReception> findById(Long id);

    @EntityGraph(attributePaths = {
            "materialOrder",
            "materialOrder.lines",
            "materialOrder.lines.material",
            "materialOrder.materialProvider",
            "materialOrderLine",
            "materialOrderLine.material",
            "deliveryNote"
    })
    List<MaterialOrderReception> findByMaterialOrder_Id(Long materialOrderId);

    boolean existsByMaterialOrder_Id(Long materialOrderId);

    boolean existsByDeliveryNote_Id(Long deliveryNoteId);

    @Query("""
            SELECT r.materialOrderLine.id FROM MaterialOrderReception r
            WHERE r.materialOrder.id = :materialOrderId
              AND r.materialOrderLine IS NOT NULL
            """)
    Set<Long> findReceivedLineIdsByMaterialOrderId(@Param("materialOrderId") Long materialOrderId);

    @EntityGraph(attributePaths = {
            "materialOrder",
            "materialOrder.lines",
            "materialOrder.lines.material",
            "materialOrder.materialProvider",
            "materialOrderLine",
            "materialOrderLine.material",
            "deliveryNote"
    })
    Optional<MaterialOrderReception> findFirstByMaterialOrder_Id(Long materialOrderId);
}
