package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.service.LicenseActivationService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.LicenseActivationInfoTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/license")
@CrossOrigin(origins = "*")
public class LicenseFacade {

    private final LicenseActivationService licenseActivationService;

    @Autowired
    public LicenseFacade(LicenseActivationService licenseActivationService) {
        this.licenseActivationService = licenseActivationService;
    }

    @GetMapping("/activation-info")
    public ResponseEntity<LicenseActivationInfoTO> getActivationInfo() {
        return ResponseEntity.ok(licenseActivationService.getActivationInfo());
    }
}
