package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialProviderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialProviderTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialProviderRepository materialProviderRepository;

    public MaterialService(
            MaterialRepository materialRepository,
            MaterialProviderRepository materialProviderRepository) {
        this.materialRepository = materialRepository;
        this.materialProviderRepository = materialProviderRepository;
    }

    public List<Material> getAllMaterials() {
        return materialRepository.findAll();
    }

    @Transactional
    public Material saveMaterialFromTo(MaterialTO to) throws Exception {
        Material entity;
        if (to.getId() != null && to.getId() > 0) {
            entity = materialRepository.findById(to.getId())
                    .orElseThrow(() -> new Exception("MATERIAL_NOT_FOUND"));
        } else {
            entity = new Material();
        }
        entity.setName(to.getName());
        entity.setCode(to.getCode());
        if (entity.getProviders() == null) {
            entity.setProviders(new ArrayList<>());
        }
        entity.getProviders().clear();
        if (to.getProviders() != null) {
            for (MaterialProviderTO pto : to.getProviders()) {
                if (pto.getId() == null) {
                    continue;
                }
                MaterialProvider p = materialProviderRepository.findById(pto.getId()).orElse(null);
                if (p != null) {
                    entity.getProviders().add(p);
                }
            }
        }
        Material saved = materialRepository.save(entity);
        return materialRepository.findById(saved.getId()).orElse(saved);
    }

    @Transactional
    public void deleteMaterial(Long id) throws Exception {
        if (id == null || id <= 0 || !materialRepository.existsById(id)) {
            throw new Exception("MATERIAL_NOT_FOUND");
        }
        materialRepository.deleteById(id);
    }
}

