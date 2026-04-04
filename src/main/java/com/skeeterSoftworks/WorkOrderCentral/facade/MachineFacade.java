package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Machine;
import com.skeeterSoftworks.WorkOrderCentral.mapper.MachineMapperService;
import com.skeeterSoftworks.WorkOrderCentral.service.MachineDeleteBlockedException;
import com.skeeterSoftworks.WorkOrderCentral.service.MachineService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ApiErrorTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MachineTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/machines")
@CrossOrigin(origins = "*")
public class MachineFacade {

    private final MachineService machineService;
    private final MachineMapperService machineMapperService;

    @Autowired
    public MachineFacade(MachineService machineService, MachineMapperService machineMapperService) {
        this.machineService = machineService;
        this.machineMapperService = machineMapperService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            List<Machine> all = machineService.getAllMachines();
            return ResponseEntity.ok(all.stream().map(machineMapperService::mapToTO).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_MACHINES");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return machineService.getMachineById(id)
                    .map(machineMapperService::mapToTO)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_MACHINE");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody MachineTO machineTO) {
        log.debug("Facade call: addMachine");
        try {
            Machine entity = machineMapperService.mapToEntity(machineTO);
            Machine saved = machineService.addMachine(entity);
            return ResponseEntity.ok(machineMapperService.mapToTO(saved));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_SAVING_MACHINE");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody MachineTO machineTO) {
        log.debug("Facade call: updateMachine");
        if (machineTO.getId() == null || machineTO.getId() <= 0) {
            log.error("Invalid id for update: {}", machineTO.getId());
            return ResponseEntity.badRequest().body("INVALID_ID");
        }
        try {
            Machine entity = machineMapperService.mapToEntity(machineTO);
            if (machineTO.getMachineImageBase64() == null) {
                machineService.getMachineById(machineTO.getId())
                        .ifPresent(existing -> entity.setMachineImage(existing.getMachineImage()));
            }
            Machine updated = machineService.updateMachine(entity);
            return ResponseEntity.ok(machineMapperService.mapToTO(updated));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            machineService.deleteMachine(id);
            return ResponseEntity.ok().build();
        } catch (MachineDeleteBlockedException e) {
            log.warn("Machine {} delete blocked: {} linked product(s)", id, e.getLinkedProductCount());
            return ResponseEntity.status(409).body(new ApiErrorTO(
                    "errorMachineDeleteLinkedProducts",
                    Map.of("count", e.getLinkedProductCount())
            ));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if ("MACHINE_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(404).body("MACHINE_NOT_FOUND");
            }
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
