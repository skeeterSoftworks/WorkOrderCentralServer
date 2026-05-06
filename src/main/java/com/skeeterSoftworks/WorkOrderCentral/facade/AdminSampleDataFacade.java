package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.service.SampleDataGenerationService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.SampleDataGenerationResultTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/sample-data")
@CrossOrigin(origins = "*")
public class AdminSampleDataFacade {

    private final SampleDataGenerationService sampleDataGenerationService;

    public AdminSampleDataFacade(SampleDataGenerationService sampleDataGenerationService) {
        this.sampleDataGenerationService = sampleDataGenerationService;
    }

    /**
     * Inserts 10 demo rows each: machines, tools, products, customers, users, materials, material providers
     * (same rules as manual test).
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generate() {
        try {
            SampleDataGenerationResultTO result = sampleDataGenerationService.generateDemoBatch();
            log.info(
                    "Sample data generated via API: {} machines, {} tools, {} products, {} customers, {} users, {} materials, {} material providers",
                    result.getMachinesInserted(),
                    result.getToolsInserted(),
                    result.getProductsInserted(),
                    result.getCustomersInserted(),
                    result.getUsersInserted(),
                    result.getMaterialsInserted(),
                    result.getMaterialProvidersInserted());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage() != null ? e.getMessage() : "ERROR_GENERATING_SAMPLE_DATA");
        }
    }
}
