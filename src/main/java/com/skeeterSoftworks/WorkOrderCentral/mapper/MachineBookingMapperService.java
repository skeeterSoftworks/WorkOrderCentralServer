package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Machine;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MachineBooking;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MachineRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.WorkOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MachineBookingTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MachineBookingMapperService {

    private final MachineRepository machineRepository;
    private final WorkOrderRepository workOrderRepository;

    @Autowired
    public MachineBookingMapperService(MachineRepository machineRepository, WorkOrderRepository workOrderRepository) {
        this.machineRepository = machineRepository;
        this.workOrderRepository = workOrderRepository;
    }

    public MachineBookingTO mapToTO(MachineBooking booking) {
        if (booking == null) return null;
        MachineBookingTO to = new MachineBookingTO();
        to.setId(booking.getId());
        to.setMachineId(booking.getMachine() != null ? booking.getMachine().getId() : null);
        to.setWorkOrderId(booking.getWorkOrder() != null ? booking.getWorkOrder().getId() : null);
        to.setStartDateTime(booking.getStartDateTime());
        to.setEndDateTime(booking.getEndDateTime());
        to.setType(booking.getType());
        to.setStatus(booking.getStatus());
        to.setComment(booking.getComment());
        return to;
    }

    public MachineBooking mapToEntity(MachineBookingTO to) {
        if (to == null) return null;
        MachineBooking booking = new MachineBooking();
        if (to.getId() != null) {
            booking.setId(to.getId());
        }
        if (to.getMachineId() != null) {
            machineRepository.findById(to.getMachineId()).ifPresent(booking::setMachine);
        }
        if (to.getWorkOrderId() != null) {
            workOrderRepository.findById(to.getWorkOrderId()).ifPresent(booking::setWorkOrder);
        }
        booking.setStartDateTime(to.getStartDateTime());
        booking.setEndDateTime(to.getEndDateTime());
        booking.setType(to.getType());
        booking.setStatus(to.getStatus());
        booking.setComment(to.getComment());
        return booking;
    }
}

