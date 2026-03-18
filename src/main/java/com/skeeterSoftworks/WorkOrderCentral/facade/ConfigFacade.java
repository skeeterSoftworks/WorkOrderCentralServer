package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.service.ConfigFilesLoaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
