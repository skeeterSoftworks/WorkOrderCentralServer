package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderReception;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderReceptionInternalControl;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialOrderReceptionRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMaterialOrderStatus;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderReceptionInternalControlTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderReceptionTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialReceptionStockAllocationTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MaterialOrderReceptionService {

    private static final int REQUIRED_SAMPLES = 3;

    private final MaterialOrderReceptionRepository materialOrderReceptionRepository;
    private final MaterialOrderRepository materialOrderRepository;
    private final StockInventoryService stockInventoryService;

    public MaterialOrderReceptionService(
            MaterialOrderReceptionRepository materialOrderReceptionRepository,
            MaterialOrderRepository materialOrderRepository,
            StockInventoryService stockInventoryService) {
        this.materialOrderReceptionRepository = materialOrderReceptionRepository;
        this.materialOrderRepository = materialOrderRepository;
        this.stockInventoryService = stockInventoryService;
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

    public List<MaterialOrderReception> getPendingValidation() {
        return materialOrderReceptionRepository.findAll().stream()
                .filter(r -> r.getMaterialOrder() != null
                        && r.getMaterialOrder().getStatus() == EMaterialOrderStatus.RECEIVED_IN_STOCK)
                .toList();
    }

    @Transactional
    public MaterialOrderReception submitInternalControl(Long receptionId, MaterialOrderReceptionInternalControlTO body)
            throws Exception {
        MaterialOrderReception reception = materialOrderReceptionRepository.findById(receptionId)
                .orElseThrow(() -> new Exception("MATERIAL_ORDER_RECEPTION_NOT_FOUND"));
        MaterialOrder order = reception.getMaterialOrder();
        if (order == null) {
            throw new Exception("MATERIAL_ORDER_NOT_FOUND");
        }
        if (order.getStatus() != EMaterialOrderStatus.RECEIVED_IN_STOCK) {
            throw new Exception("MATERIAL_ORDER_NOT_PENDING_VALIDATION");
        }

        Material material = order.getMaterial();
        if (material == null) {
            throw new Exception("MATERIAL_NOT_FOUND");
        }

        MaterialOrderReceptionInternalControl ic = reception.getInternalControl();
        if (ic == null) {
            ic = new MaterialOrderReceptionInternalControl();
            reception.setInternalControl(ic);
        }

        if (isDimensionDefined(material.getDiameter())) {
            ic.setDiameterSamples(parseSamples(body != null ? body.getDiameterSamples() : null, "DIAMETER"));
        }
        if (isDimensionDefined(material.getLength())) {
            ic.setLengthSamples(parseSamples(body != null ? body.getLengthSamples() : null, "LENGTH"));
        }
        if (isDimensionDefined(material.getWidth())) {
            ic.setWidthSamples(parseSamples(body != null ? body.getWidthSamples() : null, "WIDTH"));
        }

        if (body == null || body.getOverallWeight() == null || !Float.isFinite(body.getOverallWeight())) {
            throw new Exception("MATERIAL_ORDER_RECEPTION_OVERALL_WEIGHT_REQUIRED");
        }
        if (body.getOverallAcceptance() == null) {
            throw new Exception("MATERIAL_ORDER_RECEPTION_OVERALL_ACCEPTANCE_REQUIRED");
        }
        ic.setOverallWeight(body.getOverallWeight());
        ic.setOverallAcceptance(body.getOverallAcceptance());

        MaterialOrderReception saved = materialOrderReceptionRepository.save(reception);

        order.setStatus(EMaterialOrderStatus.VALIDATED);
        order.setLastChanged(LocalDateTime.now());
        materialOrderRepository.save(order);

        return saved;
    }

    private static boolean isDimensionDefined(float value) {
        return value != 0f;
    }

    private static List<Float> parseSamples(List<Float> samples, String dimensionLabel) throws Exception {
        if (samples == null || samples.size() < REQUIRED_SAMPLES) {
            throw new Exception("MATERIAL_ORDER_RECEPTION_SAMPLES_REQUIRED_" + dimensionLabel);
        }
        List<Float> parsed = samples.stream()
                .limit(REQUIRED_SAMPLES)
                .collect(Collectors.toCollection(ArrayList::new));
        for (Float sample : parsed) {
            if (sample == null || !Float.isFinite(sample)) {
                throw new Exception("MATERIAL_ORDER_RECEPTION_SAMPLES_INVALID_" + dimensionLabel);
            }
        }
        return parsed;
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
        if (order.getMaterial() == null || order.getMaterial().getId() == null) {
            throw new Exception("MATERIAL_NOT_FOUND");
        }
        if (order.getCertificate() == null || order.getCertificate().length == 0) {
            throw new Exception("MATERIAL_ORDER_CERTIFICATE_REQUIRED");
        }

        MaterialOrderReception reception = new MaterialOrderReception();
        reception.setMaterialOrder(order);
        reception.setReceivedAt(to.getReceivedAt());
        reception.setReceivedQuantity(to.getReceivedQuantity());
        reception.setInternalControl(new MaterialOrderReceptionInternalControl());

        MaterialOrderReception saved = materialOrderReceptionRepository.save(reception);

        stockInventoryService.applyReceptionStockAllocations(
                order.getMaterial(),
                to.getReceivedQuantity(),
                to.getStockAllocations());

        order.setStatus(EMaterialOrderStatus.RECEIVED_IN_STOCK);
        order.setLastChanged(LocalDateTime.now());
        materialOrderRepository.save(order);

        return saved;
    }
}
