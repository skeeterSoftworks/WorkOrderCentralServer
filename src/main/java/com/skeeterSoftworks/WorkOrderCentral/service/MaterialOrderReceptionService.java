package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderReception;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialOrderReceptionRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMaterialOrderStatus;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderReceptionTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MaterialOrderReceptionService {

    private final MaterialOrderReceptionRepository materialOrderReceptionRepository;
    private final MaterialOrderRepository materialOrderRepository;

    public MaterialOrderReceptionService(
            MaterialOrderReceptionRepository materialOrderReceptionRepository,
            MaterialOrderRepository materialOrderRepository) {
        this.materialOrderReceptionRepository = materialOrderReceptionRepository;
        this.materialOrderRepository = materialOrderRepository;
    }

    public List<MaterialOrderReception> getAll() {
        return materialOrderReceptionRepository.findAll();
    }

    public Optional<MaterialOrderReception> getById(Long id) {
        return materialOrderReceptionRepository.findById(id);
    }

    public List<MaterialOrderReception> getByMaterialOrderId(Long materialOrderId) {
        return materialOrderReceptionRepository.findByMaterialOrder_Id(materialOrderId);
    }

    @Transactional
    public MaterialOrderReception recordReception(MaterialOrderReceptionTO to) throws Exception {
        if (to == null || to.getMaterialOrderId() == null || to.getMaterialOrderId() <= 0) {
            throw new Exception("MATERIAL_ORDER_RECEPTION_ORDER_REQUIRED");
        }
        if (to.getReceivedAt() == null) {
            throw new Exception("MATERIAL_ORDER_RECEPTION_DATE_REQUIRED");
        }
        if (to.getReceivedQuantity() == null || to.getReceivedQuantity() <= 0) {
            throw new Exception("MATERIAL_ORDER_RECEPTION_INVALID_QUANTITY");
        }

        MaterialOrder order = materialOrderRepository.findById(to.getMaterialOrderId())
                .orElseThrow(() -> new Exception("MATERIAL_ORDER_NOT_FOUND"));

        if (order.getStatus() == EMaterialOrderStatus.RECEIVED_IN_STOCK
                || order.getStatus() == EMaterialOrderStatus.VALIDATED) {
            throw new Exception("MATERIAL_ORDER_ALREADY_RECEIVED");
        }
        if (order.getStatus() != EMaterialOrderStatus.IN_TRANSPORT) {
            throw new Exception("MATERIAL_ORDER_NOT_OPEN_FOR_RECEPTION");
        }
        if (to.getReceivedQuantity() != order.getQuantity()) {
            throw new Exception("MATERIAL_ORDER_RECEPTION_QUANTITY_MISMATCH");
        }

        MaterialOrderReception reception = new MaterialOrderReception();
        reception.setMaterialOrder(order);
        reception.setReceivedAt(to.getReceivedAt());
        reception.setReceivedQuantity(to.getReceivedQuantity());

        MaterialOrderReception saved = materialOrderReceptionRepository.save(reception);

        order.setStatus(EMaterialOrderStatus.RECEIVED_IN_STOCK);
        order.setLastChanged(LocalDateTime.now());
        materialOrderRepository.save(order);

        return saved;
    }
}
