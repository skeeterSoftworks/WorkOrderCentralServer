package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialProviderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMaterialOrderStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MaterialOrderService {

    private final MaterialOrderRepository materialOrderRepository;
    private final MaterialRepository materialRepository;
    private final MaterialProviderRepository materialProviderRepository;

    public MaterialOrderService(
            MaterialOrderRepository materialOrderRepository,
            MaterialRepository materialRepository,
            MaterialProviderRepository materialProviderRepository
    ) {
        this.materialOrderRepository = materialOrderRepository;
        this.materialRepository = materialRepository;
        this.materialProviderRepository = materialProviderRepository;
    }

    public List<MaterialOrder> getAllMaterialOrders() {
        return materialOrderRepository.findAll();
    }

    public Optional<MaterialOrder> getMaterialOrderById(Long id) {
        return materialOrderRepository.findById(id);
    }

    public MaterialOrder addMaterialOrder(MaterialOrder order) throws Exception {
        order.setId(0);
        // Creation flow owns initial state; clients must not set this.
        order.setStatus(EMaterialOrderStatus.ORDER_CREATED);
        order.setCertificate(null);
        validate(order);
        return materialOrderRepository.save(order);
    }

    private void validate(MaterialOrder order) throws Exception {
        if (order.getQuantity() <= 0) {
            throw new Exception("MATERIAL_ORDER_INVALID_QUANTITY");
        }
        if (order.getMaterial() == null || order.getMaterial().getId() == null || order.getMaterial().getId() <= 0) {
            throw new Exception("MATERIAL_ORDER_MATERIAL_REQUIRED");
        }
        Material material = materialRepository.findById(order.getMaterial().getId()).orElse(null);
        if (material == null) {
            throw new Exception("MATERIAL_NOT_FOUND");
        }
        order.setMaterial(material);

        if (order.getMaterialProvider() == null
                || order.getMaterialProvider().getId() == null
                || order.getMaterialProvider().getId() <= 0) {
            throw new Exception("MATERIAL_ORDER_PROVIDER_REQUIRED");
        }
        MaterialProvider provider = materialProviderRepository.findById(order.getMaterialProvider().getId()).orElse(null);
        if (provider == null) {
            throw new Exception("MATERIAL_PROVIDER_NOT_FOUND");
        }

        boolean providerAttachedToMaterial = material.getProviders() != null
                && material.getProviders().stream().anyMatch(p -> p.getId() != null && p.getId().equals(provider.getId()));
        if (!providerAttachedToMaterial) {
            throw new Exception("MATERIAL_ORDER_PROVIDER_NOT_ALLOWED_FOR_MATERIAL");
        }
        order.setMaterialProvider(provider);
    }
}

