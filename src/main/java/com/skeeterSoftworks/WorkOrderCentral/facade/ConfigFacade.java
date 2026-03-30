package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.service.ConfigFilesLoaderService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.SelectOptionsTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/config")
public class ConfigFacade {

    @Autowired
    ConfigFilesLoaderService configFilesLoaderService;

    @GetMapping("/get-wo-preconditions")
    ResponseEntity<String> getWorkOrderPreconditions() {

        log.debug("Facade call: getWorkOrderPreconditions()");

        try {
            return ResponseEntity.ok(configFilesLoaderService.getWorkOrderPreconditions().toPrettyString());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/select-options")
    ResponseEntity<SelectOptionsTO> getSelectOptions() {
        log.debug("Facade call: getSelectOptions()");
        try {
            return ResponseEntity.ok(configFilesLoaderService.readSelectOptions());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/select-options")
    ResponseEntity<?> putSelectOptions(@RequestBody SelectOptionsTO body) {
        log.debug("Facade call: putSelectOptions()");
        if (body == null) {
            return ResponseEntity.badRequest().body("EMPTY_BODY");
        }
        try {
            configFilesLoaderService.writeSelectOptions(body);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_SAVING_SELECT_OPTIONS");
        }
    }
}
