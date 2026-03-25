package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkSessionRepository extends JpaRepository<WorkSession, Long> {

    @Query("SELECT COALESCE(SUM(s.productCount), 0) FROM WorkSession s WHERE s.workOrder.id = :workOrderId")
    long sumProductCountByWorkOrderId(@Param("workOrderId") Long workOrderId);
}
