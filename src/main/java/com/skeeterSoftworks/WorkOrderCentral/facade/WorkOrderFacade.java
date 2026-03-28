package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.mapper.WorkOrderMapperService;
import com.skeeterSoftworks.WorkOrderCentral.service.WorkOrderService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.QualityInfoStepTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.WorkOrderTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/workorders")
@CrossOrigin(origins = "*")
public class WorkOrderFacade {

    private final WorkOrderService workOrderService;
    private final WorkOrderMapperService workOrderMapperService;

    @Autowired
    public WorkOrderFacade(WorkOrderService workOrderService, WorkOrderMapperService workOrderMapperService) {
        this.workOrderService = workOrderService;
        this.workOrderMapperService = workOrderMapperService;
    }

    @GetMapping("/for-machine/{machineId}")
    public ResponseEntity<?> getForMachine(@PathVariable Long machineId) {
        try {
            if (machineId == null || machineId <= 0) {
                return ResponseEntity.badRequest().body("INVALID_MACHINE_ID");
            }
            List<WorkOrder> list = workOrderService.getWorkOrdersForMachine(machineId);
            return ResponseEntity.ok(list.stream().map(workOrderMapperService::mapToTO).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_WORK_ORDERS_FOR_MACHINE");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            List<WorkOrder> all = workOrderService.getAllWorkOrders();
            return ResponseEntity.ok(all.stream().map(workOrderMapperService::mapToTO).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_WORK_ORDERS");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return workOrderService.getWorkOrderById(id)
                    .map(workOrderMapperService::mapToTO)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_WORK_ORDER");
        }
    }

    @GetMapping("/{id}/quality-info-steps")
    public ResponseEntity<?> getQualityInfoSteps(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("INVALID_ID");
            }
            List<QualityInfoStepTO> steps = workOrderService.getQualityInfoStepsForWorkOrder(id);
            return ResponseEntity.ok(steps);
        } catch (Exception e) {
            String msg = e.getMessage();
            if ("WORK_ORDER_NOT_FOUND".equals(msg)) {
                return ResponseEntity.notFound().build();
            }
            if ("INVALID_WORK_ORDER_ID".equals(msg)) {
                return ResponseEntity.badRequest().body(msg);
            }
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_QUALITY_INFO_STEPS");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody WorkOrderTO workOrderTO) {
        log.debug("Facade call: addWorkOrder");
        try {
            WorkOrder entity = workOrderMapperService.mapToEntity(workOrderTO);
            WorkOrder saved = workOrderService.addWorkOrder(entity);
            return ResponseEntity.ok(workOrderMapperService.mapToTO(saved));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String msg = e.getMessage();
            if ("INVALID_PRODUCT_ORDER".equals(msg)
                    || "PRODUCT_ORDER_NOT_FOUND".equals(msg)
                    || "WORK_ORDER_ALREADY_EXISTS_FOR_PRODUCT_ORDER".equals(msg)) {
                return ResponseEntity.badRequest().body(msg);
            }
            return ResponseEntity.internalServerError().body("ERROR_SAVING_WORK_ORDER");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody WorkOrderTO workOrderTO) {
        log.debug("Facade call: updateWorkOrder");
        if (workOrderTO.getId() == null || workOrderTO.getId() <= 0) {
            log.error("Invalid id for update: {}", workOrderTO.getId());
            return ResponseEntity.badRequest().body("INVALID_ID");
        }
        try {
            WorkOrder entity = workOrderMapperService.mapToEntity(workOrderTO);
            WorkOrder updated = workOrderService.updateWorkOrder(entity);
            return ResponseEntity.ok(workOrderMapperService.mapToTO(updated));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String msg = e.getMessage();
            if ("INVALID_PRODUCT_ORDER".equals(msg)
                    || "PRODUCT_ORDER_NOT_FOUND".equals(msg)
                    || "WORK_ORDER_ALREADY_EXISTS_FOR_PRODUCT_ORDER".equals(msg)) {
                return ResponseEntity.badRequest().body(msg);
            }
            return ResponseEntity.internalServerError().body(msg != null ? msg : "ERROR_UPDATING_WORK_ORDER");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            workOrderService.deleteWorkOrder(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
