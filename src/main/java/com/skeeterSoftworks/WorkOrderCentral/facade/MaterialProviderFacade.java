package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import com.skeeterSoftworks.WorkOrderCentral.service.MaterialProviderService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialProviderTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/material-providers")
@CrossOrigin(origins = "*")
public class MaterialProviderFacade {

    private final MaterialProviderService materialProviderService;

    public MaterialProviderFacade(MaterialProviderService materialProviderService) {
        this.materialProviderService = materialProviderService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            List<MaterialProvider> all = materialProviderService.getAllMaterialProviders();
            return ResponseEntity.ok(all.stream().map(this::toTO).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_MATERIAL_PROVIDERS");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return materialProviderService.getMaterialProviderById(id)
                    .map(this::toTO)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_MATERIAL_PROVIDER");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody MaterialProviderTO materialProviderTO) {
        try {
            MaterialProvider saved = materialProviderService.addMaterialProvider(toEntity(materialProviderTO));
            return ResponseEntity.ok(toTO(saved));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_SAVING_MATERIAL_PROVIDER");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody MaterialProviderTO materialProviderTO) {
        if (materialProviderTO.getId() == null || materialProviderTO.getId() <= 0) {
            return ResponseEntity.badRequest().body("INVALID_ID");
        }
        try {
            MaterialProvider updated = materialProviderService.updateMaterialProvider(toEntity(materialProviderTO));
            return ResponseEntity.ok(toTO(updated));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            materialProviderService.deleteMaterialProvider(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    private MaterialProviderTO toTO(MaterialProvider p) {
        return new MaterialProviderTO(
                p.getId(),
                p.getName(),
                p.getContactPerson(),
                p.getEmailAddress(),
                p.getPhoneNumber(),
                p.getGrade()
        );
    }

    private MaterialProvider toEntity(MaterialProviderTO to) {
        MaterialProvider p = new MaterialProvider();
        p.setId(to.getId());
        p.setName(to.getName());
        p.setContactPerson(to.getContactPerson());
        p.setEmailAddress(to.getEmailAddress());
        p.setPhoneNumber(to.getPhoneNumber());
        p.setGrade(to.getGrade() != null ? to.getGrade() : 0);
        return p;
    }
}
