package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialProviderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MaterialProviderService {

    private final MaterialProviderRepository materialProviderRepository;
    private final MaterialRepository materialRepository;

    public MaterialProviderService(
            MaterialProviderRepository materialProviderRepository,
            MaterialRepository materialRepository) {
        this.materialProviderRepository = materialProviderRepository;
        this.materialRepository = materialRepository;
    }

    public List<MaterialProvider> getAllMaterialProviders() {
        return materialProviderRepository.findAll();
    }

    public Optional<MaterialProvider> getMaterialProviderById(Long id) {
        return materialProviderRepository.findById(id);
    }

    public MaterialProvider addMaterialProvider(MaterialProvider provider) {
        provider.setId(null);
        return materialProviderRepository.save(provider);
    }

    public MaterialProvider updateMaterialProvider(MaterialProvider provider) throws Exception {
        if (provider.getId() == null || provider.getId() <= 0 || !materialProviderRepository.existsById(provider.getId())) {
            throw new Exception("MATERIAL_PROVIDER_NOT_FOUND");
        }
        return materialProviderRepository.save(provider);
    }

    @Transactional
    public void deleteMaterialProvider(Long id) throws Exception {
        MaterialProvider existing = materialProviderRepository.findById(id).orElse(null);
        if (existing == null) {
            throw new Exception("MATERIAL_PROVIDER_NOT_FOUND");
        }

        List<Material> changedMaterials = new ArrayList<>();
        for (Material material : materialRepository.findAll()) {
            if (material.getProviders() == null) continue;
            boolean removed = material.getProviders().removeIf(p -> p.getId() != null && p.getId().equals(id));
            if (removed) changedMaterials.add(material);
        }
        for (Material material : changedMaterials) {
            materialRepository.save(material);
        }

        materialProviderRepository.deleteById(id);
    }
}
