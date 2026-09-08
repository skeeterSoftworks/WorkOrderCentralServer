package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkSessionRepository extends JpaRepository<WorkSession, Long> {

    @Query("SELECT COALESCE(SUM(s.productCount), 0) FROM WorkSession s WHERE s.workOrder.id = :workOrderId")
    long sumProductCountByWorkOrderId(@Param("workOrderId") Long workOrderId);

    @Query("""
            SELECT s FROM WorkSession s
            WHERE s.sessionStart >= :fromInclusive
              AND s.sessionStart < :toExclusive
              AND (:stationId IS NULL OR s.stationInfo.stationID = :stationId)
              AND (:operatorQrCode IS NULL OR s.operator.operatorQrCode = :operatorQrCode)
            ORDER BY s.sessionStart DESC
            """)
    List<WorkSession> findOverview(
            @Param("fromInclusive") LocalDateTime fromInclusive,
            @Param("toExclusive") LocalDateTime toExclusive,
            @Param("stationId") String stationId,
            @Param("operatorQrCode") String operatorQrCode);
}
