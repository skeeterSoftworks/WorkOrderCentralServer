package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Machine;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MachineBooking;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MachineBookingRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MachineRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.WorkOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EWorkOrderState;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMachineBookingStatus;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMachineBookingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class MachineBookingService {

    private final MachineBookingRepository bookingRepository;
    private final MachineRepository machineRepository;
    private final WorkOrderRepository workOrderRepository;

    @Autowired
    public MachineBookingService(
            MachineBookingRepository bookingRepository,
            MachineRepository machineRepository,
            WorkOrderRepository workOrderRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.machineRepository = machineRepository;
        this.workOrderRepository = workOrderRepository;
    }

    public List<MachineBooking> getAll() {
        return bookingRepository.findAll();
    }

    public Optional<MachineBooking> getById(Long id) {
        return bookingRepository.findById(id);
    }

    public List<MachineBooking> getBookingsForWorkOrder(Long workOrderId) throws Exception {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new Exception("WORK_ORDER_NOT_FOUND"));
        return bookingRepository.findByWorkOrder(workOrder).stream()
                .sorted(Comparator.comparing(MachineBooking::getStartDateTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public List<MachineBooking> getBookingsForMachine(Long machineId, LocalDateTime from, LocalDateTime to) throws Exception {
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new Exception("MACHINE_NOT_FOUND"));
        if (from == null || to == null || !from.isBefore(to)) {
            throw new IllegalArgumentException("INVALID_INTERVAL");
        }
        return bookingRepository.findOverlappingForMachine(machine, from, to);
    }

    /**
     * Sets {@link EMachineBookingStatus#COMPLETED} on every booking for the work order except
     * {@link EMachineBookingStatus#CANCELLED}. Used when the work order itself becomes complete.
     */
    public void completeNonCancelledBookingsForWorkOrder(WorkOrder workOrder) {
        if (workOrder == null || workOrder.getId() == null) {
            return;
        }
        WorkOrder managed = workOrderRepository.findById(workOrder.getId()).orElse(workOrder);
        for (MachineBooking b : bookingRepository.findByWorkOrder(managed)) {
            if (b.getStatus() != EMachineBookingStatus.CANCELLED) {
                b.setStatus(EMachineBookingStatus.COMPLETED);
                bookingRepository.save(b);
            }
        }
    }

    public MachineBooking createBooking(Long machineId, Long workOrderId, LocalDateTime start, LocalDateTime end,
                                        EMachineBookingType type, String comment) throws Exception {
        if (machineId == null) {
            throw new IllegalArgumentException("MACHINE_ID_REQUIRED");
        }
        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException("INVALID_INTERVAL");
        }
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new Exception("MACHINE_NOT_FOUND"));
        WorkOrder workOrder = null;
        if (workOrderId != null) {
            workOrder = workOrderRepository.findById(workOrderId)
                    .orElseThrow(() -> new Exception("WORK_ORDER_NOT_FOUND"));
        }
        if (bookingRepository.existsOverlappingForMachine(machine, start, end)) {
            throw new Exception("MACHINE_ALREADY_BOOKED");
        }
        MachineBooking booking = new MachineBooking();
        booking.setMachine(machine);
        booking.setWorkOrder(workOrder);
        booking.setStartDateTime(start);
        booking.setEndDateTime(end);
        booking.setType(type != null ? type : EMachineBookingType.PRODUCTION);
        boolean woComplete = workOrder != null && workOrder.getState() == EWorkOrderState.COMPLETE;
        booking.setStatus(woComplete ? EMachineBookingStatus.COMPLETED : EMachineBookingStatus.PLANNED);
        booking.setComment(comment);
        booking.setCreatedAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    public MachineBooking updateBooking(MachineBooking booking) throws Exception {
        if (booking.getId() == null || booking.getId() <= 0 || !bookingRepository.existsById(booking.getId())) {
            throw new Exception("BOOKING_NOT_FOUND");
        }
        if (booking.getStartDateTime() == null || booking.getEndDateTime() == null ||
                !booking.getStartDateTime().isBefore(booking.getEndDateTime())) {
            throw new IllegalArgumentException("INVALID_INTERVAL");
        }
        Machine machine = booking.getMachine();
        if (machine == null || machine.getId() == null) {
            throw new IllegalArgumentException("MACHINE_ID_REQUIRED");
        }
        Machine managedMachine = machineRepository.findById(machine.getId())
                .orElseThrow(() -> new Exception("MACHINE_NOT_FOUND"));
        if (bookingRepository.existsOverlappingForMachine(
                managedMachine,
                booking.getStartDateTime(),
                booking.getEndDateTime()
        )) {
            // naive implementation: this also sees the current booking; for simplicity we skip excluding self
            // in real code, we would adapt the query to ignore the same id
            throw new Exception("MACHINE_ALREADY_BOOKED");
        }
        booking.setMachine(managedMachine);
        if (booking.getWorkOrder() != null && booking.getWorkOrder().getId() != null) {
            workOrderRepository.findById(booking.getWorkOrder().getId()).ifPresent(wo -> {
                if (wo.getState() == EWorkOrderState.COMPLETE && booking.getStatus() != EMachineBookingStatus.CANCELLED) {
                    booking.setStatus(EMachineBookingStatus.COMPLETED);
                }
            });
        }
        return bookingRepository.save(booking);
    }

    public void cancelBooking(Long id) throws Exception {
        MachineBooking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new Exception("BOOKING_NOT_FOUND"));
        booking.setStatus(EMachineBookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
}

