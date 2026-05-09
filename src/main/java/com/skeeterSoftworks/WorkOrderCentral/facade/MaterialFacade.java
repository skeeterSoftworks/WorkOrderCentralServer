package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import com.skeeterSoftworks.WorkOrderCentral.service.MaterialService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialProviderTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/materials")
@CrossOrigin(origins = "*")
public class MaterialFacade {

    private final MaterialService materialService;

    public MaterialFacade(MaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            List<Material> all = materialService.getAllMaterials();
            return ResponseEntity.ok(all.stream().map(this::toTO).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_MATERIALS");
        }
    }

    private MaterialTO toTO(Material m) {
        return new MaterialTO(
                m.getId(),
                m.getName(),
                m.getCode(),
                m.getProductsPerUnit(),
                m.getDiameter(),
                m.getWeight(),
                m.getLength(),
                m.getWidth(),
                (m.getProviders() == null ? List.<MaterialProviderTO>of() : m.getProviders().stream().map(this::toProviderTO).toList())
        );
    }

    private MaterialProviderTO toProviderTO(MaterialProvider p) {
        return new MaterialProviderTO(
                p.getId(),
                p.getName(),
                p.getContactPerson(),
                p.getEmailAddress(),
                p.getPhoneNumber(),
                p.getGrade()
        );
    }
}

