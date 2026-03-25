package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkSession;
import com.skeeterSoftworks.WorkOrderCentral.mapper.WorkSessionMapperService;
import com.skeeterSoftworks.WorkOrderCentral.service.WorkSessionIncrementResult;
import com.skeeterSoftworks.WorkOrderCentral.service.WorkSessionService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/work-sessions")
@CrossOrigin(origins = "*")
public class WorkSessionFacade {

    private final WorkSessionService workSessionService;
    private final WorkSessionMapperService workSessionMapperService;

    @Autowired
    public WorkSessionFacade(WorkSessionService workSessionService, WorkSessionMapperService workSessionMapperService) {
        this.workSessionService = workSessionService;
        this.workSessionMapperService = workSessionMapperService;
    }

    @PostMapping("/open")
    public ResponseEntity<?> open(@RequestBody WorkSessionOpenRequestTO body) {
        try {
            WorkSession saved = workSessionService.openSession(body);
            return ResponseEntity.ok(workSessionMapperService.mapToTO(saved));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String msg = e.getMessage();
            if ("INVALID_WORK_ORDER_ID".equals(msg) || "WORK_ORDER_NOT_FOUND".equals(msg)
                    || "WORK_ORDER_COMPLETE".equals(msg)) {
                return ResponseEntity.badRequest().body(msg);
            }
            return ResponseEntity.internalServerError().body("ERROR_OPENING_WORK_SESSION");
        }
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<?> end(@PathVariable Long id) {
        try {
            WorkSession saved = workSessionService.endSession(id);
            return ResponseEntity.ok(workSessionMapperService.mapToTO(saved));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String msg = e.getMessage();
            if ("WORK_SESSION_NOT_FOUND".equals(msg) || "WORK_SESSION_ALREADY_ENDED".equals(msg)) {
                return ResponseEntity.badRequest().body(msg);
            }
            return ResponseEntity.internalServerError().body("ERROR_ENDING_WORK_SESSION");
        }
    }

    @PostMapping("/{id}/product-count-delta")
    public ResponseEntity<?> productCountDelta(@PathVariable Long id, @RequestBody ProductCountDeltaRequestTO body) {
        try {
            WorkSessionIncrementResult result = workSessionService.incrementProductCount(id, body);
            return ResponseEntity.ok(workSessionMapperService.mapToTO(
                    result.session(),
                    result.workOrderCompletedByTarget()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String msg = e.getMessage();
            if ("WORK_SESSION_NOT_FOUND".equals(msg) || "WORK_SESSION_ALREADY_ENDED".equals(msg)
                    || "INVALID_DELTA".equals(msg)) {
                return ResponseEntity.badRequest().body(msg);
            }
            return ResponseEntity.internalServerError().body("ERROR_UPDATING_PRODUCT_COUNT");
        }
    }

    @PostMapping("/{id}/control-products")
    public ResponseEntity<?> addControlProduct(@PathVariable Long id, @RequestBody ControlProductCreateRequestTO body) {
        try {
            WorkSession saved = workSessionService.addControlProduct(id, body);
            return ResponseEntity.ok(workSessionMapperService.mapToTO(saved));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String msg = e.getMessage();
            if ("WORK_SESSION_NOT_FOUND".equals(msg) || "WORK_SESSION_ALREADY_ENDED".equals(msg)
                    || "MEASURING_FEATURES_REQUIRED".equals(msg) || "FEATURE_NAME_REQUIRED".equals(msg)) {
                return ResponseEntity.badRequest().body(msg);
            }
            return ResponseEntity.internalServerError().body("ERROR_ADDING_CONTROL_PRODUCT");
        }
    }

    @PostMapping("/{id}/faulty-products")
    public ResponseEntity<?> addFaultyProduct(@PathVariable Long id, @RequestBody FaultyProductCreateRequestTO body) {
        try {
            WorkSession saved = workSessionService.addFaultyProduct(id, body);
            return ResponseEntity.ok(workSessionMapperService.mapToTO(saved));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String msg = e.getMessage();
            if ("WORK_SESSION_NOT_FOUND".equals(msg) || "WORK_SESSION_ALREADY_ENDED".equals(msg)) {
                return ResponseEntity.badRequest().body(msg);
            }
            return ResponseEntity.internalServerError().body("ERROR_ADDING_FAULTY_PRODUCT");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            WorkSession session = workSessionService.getById(id);
            return ResponseEntity.ok(workSessionMapperService.mapToTO(session));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if ("WORK_SESSION_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_WORK_SESSION");
        }
    }
}
