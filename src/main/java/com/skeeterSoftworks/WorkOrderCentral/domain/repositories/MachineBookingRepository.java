package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Machine;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MachineBooking;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MachineBookingRepository extends CrudRepository<MachineBooking, Long> {

    List<MachineBooking> findAll();

    List<MachineBooking> findByMachine(Machine machine);

    @EntityGraph(attributePaths = {"machine", "workOrder"})
    List<MachineBooking> findByWorkOrder(WorkOrder workOrder);

    @Query("""
            SELECT b FROM MachineBooking b
            WHERE b.machine = :machine
              AND b.status <> com.skeeterSoftworks.WorkOrderCentral.to.enums.EMachineBookingStatus.CANCELLED
              AND b.endDateTime > :from
              AND b.startDateTime < :to
            """)
    List<MachineBooking> findOverlappingForMachine(
            @Param("machine") Machine machine,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
            SELECT COUNT(b) > 0 FROM MachineBooking b
            WHERE b.machine = :machine
              AND b.status <> com.skeeterSoftworks.WorkOrderCentral.to.enums.EMachineBookingStatus.CANCELLED
              AND b.endDateTime > :from
              AND b.startDateTime < :to
            """)
    boolean existsOverlappingForMachine(
            @Param("machine") Machine machine,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
            SELECT DISTINCT b.workOrder.id FROM MachineBooking b
            WHERE b.machine.id = :machineId
              AND b.workOrder IS NOT NULL
              AND b.status <> com.skeeterSoftworks.WorkOrderCentral.to.enums.EMachineBookingStatus.CANCELLED
            """)
    List<Long> findWorkOrderIdsScheduledOnMachine(@Param("machineId") Long machineId);
}

