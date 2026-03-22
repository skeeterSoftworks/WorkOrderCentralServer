package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MachineBooking;
import com.skeeterSoftworks.WorkOrderCentral.mapper.MachineBookingMapperService;
import com.skeeterSoftworks.WorkOrderCentral.service.MachineBookingService;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMachineBookingType;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MachineBookingTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/machine-bookings")
@CrossOrigin(origins = "*")
public class MachineBookingFacade {

    private final MachineBookingService bookingService;
    private final MachineBookingMapperService mapperService;

    @Autowired
    public MachineBookingFacade(MachineBookingService bookingService, MachineBookingMapperService mapperService) {
        this.bookingService = bookingService;
        this.mapperService = mapperService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            List<MachineBooking> all = bookingService.getAll();
            return ResponseEntity.ok(all.stream().map(mapperService::mapToTO).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_MACHINE_BOOKINGS");
        }
    }

    @GetMapping("/work-order/{workOrderId}")
    public ResponseEntity<?> getForWorkOrder(@PathVariable Long workOrderId) {
        try {
            List<MachineBooking> list = bookingService.getBookingsForWorkOrder(workOrderId);
            return ResponseEntity.ok(list.stream().map(mapperService::mapToTO).toList());
        } catch (Exception e) {
            if ("WORK_ORDER_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.notFound().build();
            }
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/machine/{machineId}")
    public ResponseEntity<?> getForMachine(
            @PathVariable Long machineId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        try {
            List<MachineBooking> list = bookingService.getBookingsForMachine(machineId, from, to);
            return ResponseEntity.ok(list.stream().map(mapperService::mapToTO).toList());
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody MachineBookingTO to) {
        log.debug("Facade call: addMachineBooking");
        try {
            MachineBooking created = bookingService.createBooking(
                    to.getMachineId(),
                    to.getWorkOrderId(),
                    to.getStartDateTime(),
                    to.getEndDateTime(),
                    to.getType() != null ? to.getType() : EMachineBookingType.PRODUCTION,
                    to.getComment()
            );
            return ResponseEntity.ok(mapperService.mapToTO(created));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody MachineBookingTO to) {
        log.debug("Facade call: updateMachineBooking");
        if (to.getId() == null || to.getId() <= 0) {
            log.error("Invalid id for update: {}", to.getId());
            return ResponseEntity.badRequest().body("INVALID_ID");
        }
        try {
            MachineBooking entity = mapperService.mapToEntity(to);
            MachineBooking updated = bookingService.updateBooking(entity);
            return ResponseEntity.ok(mapperService.mapToTO(updated));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        log.debug("Facade call: cancelMachineBooking");
        try {
            bookingService.cancelBooking(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}

