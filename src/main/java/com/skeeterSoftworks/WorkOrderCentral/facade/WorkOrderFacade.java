package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.mapper.WorkOrderMapperService;
import com.skeeterSoftworks.WorkOrderCentral.service.WorkOrderService;
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

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody WorkOrderTO workOrderTO) {
        log.debug("Facade call: addWorkOrder");
        try {
            WorkOrder entity = workOrderMapperService.mapToEntity(workOrderTO);
            WorkOrder saved = workOrderService.addWorkOrder(entity);
            return ResponseEntity.ok(workOrderMapperService.mapToTO(saved));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
            return ResponseEntity.internalServerError().body(e.getMessage());
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
