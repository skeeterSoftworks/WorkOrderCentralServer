package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialProviderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MaterialProviderService {

    private final MaterialProviderRepository materialProviderRepository;
    private final MaterialRepository materialRepository;
    private final ProductRepository productRepository;

    public MaterialProviderService(
            MaterialProviderRepository materialProviderRepository,
            MaterialRepository materialRepository,
            ProductRepository productRepository) {
        this.materialProviderRepository = materialProviderRepository;
        this.materialRepository = materialRepository;
        this.productRepository = productRepository;
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

        List<Product> changedProducts = new ArrayList<>();
        for (Product product : productRepository.findAll()) {
            if (product.getMaterialProviders() == null) continue;
            boolean removed = product.getMaterialProviders().removeIf(p -> p.getId() != null && p.getId().equals(id));
            if (removed) changedProducts.add(product);
        }
        for (Product product : changedProducts) {
            productRepository.save(product);
        }

        materialProviderRepository.deleteById(id);
    }
}
