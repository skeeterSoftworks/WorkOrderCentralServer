package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialAssignmentOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialAssignmentOrderLine;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockedMaterial;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialAssignmentOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.StockedMaterialRepository;
import com.skeeterSoftworks.WorkOrderCentral.mapper.MaterialAssignmentOrderMapperService;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EStockAssignmentOrderStatus;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialAssignmentOrderTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.WorkOrderMaterialRequirementLineTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.WorkOrderMaterialRequirementsTO;
import com.skeeterSoftworks.WorkOrderCentral.util.AssignmentOrderCodes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MaterialAssignmentInventoryService {

    private final MaterialAssignmentOrderRepository materialAssignmentOrderRepository;
    private final MaterialRepository materialRepository;
    private final StockedMaterialRepository stockedMaterialRepository;
    private final WorkOrderMaterialRequirementsService workOrderMaterialRequirementsService;
    private final MaterialAssignmentOrderMapperService materialAssignmentOrderMapperService;
    private final UsersService usersService;

    public MaterialAssignmentInventoryService(
            MaterialAssignmentOrderRepository materialAssignmentOrderRepository,
            MaterialRepository materialRepository,
            StockedMaterialRepository stockedMaterialRepository,
            WorkOrderMaterialRequirementsService workOrderMaterialRequirementsService,
            MaterialAssignmentOrderMapperService materialAssignmentOrderMapperService,
            UsersService usersService) {
        this.materialAssignmentOrderRepository = materialAssignmentOrderRepository;
        this.materialRepository = materialRepository;
        this.stockedMaterialRepository = stockedMaterialRepository;
        this.workOrderMaterialRequirementsService = workOrderMaterialRequirementsService;
        this.materialAssignmentOrderMapperService = materialAssignmentOrderMapperService;
        this.usersService = usersService;
    }

    @Transactional
    public MaterialAssignmentOrder createForWorkOrder(WorkOrder workOrder, String createdByFullName) throws Exception {
        if (workOrder == null || workOrder.getId() == null) {
            throw new Exception("WORK_ORDER_NOT_FOUND");
        }
        WorkOrderMaterialRequirementsTO requirements =
                workOrderMaterialRequirementsService.previewForWorkOrder(workOrder.getId());
        if (requirements.getLines() == null || requirements.getLines().isEmpty()) {
            return null;
        }

        MaterialAssignmentOrder order = new MaterialAssignmentOrder();
        order.setCode(generateUniqueEightDigitCode());
        order.setWorkOrder(workOrder);
        order.setStatus(EStockAssignmentOrderStatus.UNASSIGNED);
        order.setCreatedAt(LocalDateTime.now());
        order.setCreatedByFullName(StringUtils.hasText(createdByFullName) ? createdByFullName.trim() : null);

        List<MaterialAssignmentOrderLine> lines = new ArrayList<>();
        for (WorkOrderMaterialRequirementLineTO row : requirements.getLines()) {
            if (row == null || row.getMaterialId() == null || row.getMaterialId() <= 0) {
                continue;
            }
            int quantity = toIssueQuantity(row.getRequiredQuantity());
            if (quantity <= 0) {
                continue;
            }
            Material material = materialRepository.findById(row.getMaterialId())
                    .orElseThrow(() -> new Exception("MATERIAL_NOT_FOUND"));
            MaterialAssignmentOrderLine line = new MaterialAssignmentOrderLine();
            line.setAssignmentOrder(order);
            line.setMaterial(material);
            line.setQuantity(quantity);
            lines.add(line);
        }
        if (lines.isEmpty()) {
            return null;
        }
        order.setLines(lines);
        return materialAssignmentOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public MaterialAssignmentOrderTO getAssignmentOrderByCode(String code) throws Exception {
        MaterialAssignmentOrder order = findByNormalizedCode(code)
                .orElseThrow(() -> new Exception("MATERIAL_ASSIGNMENT_ORDER_NOT_FOUND"));
        return materialAssignmentOrderMapperService.mapToTO(order);
    }

    @Transactional
    public MaterialAssignmentOrderTO fulfillAssignmentOrderByCode(String code, String operatorUserQrCode) throws Exception {
        MaterialAssignmentOrder order = findByNormalizedCode(code)
                .orElseThrow(() -> new Exception("MATERIAL_ASSIGNMENT_ORDER_NOT_FOUND"));
        if (order.getStatus() == EStockAssignmentOrderStatus.ASSIGNED) {
            throw new Exception("MATERIAL_ASSIGNMENT_ORDER_ALREADY_ASSIGNED");
        }
        if (order.getLines() == null || order.getLines().isEmpty()) {
            throw new Exception("MATERIAL_ASSIGNMENT_ORDER_EMPTY");
        }

        for (MaterialAssignmentOrderLine line : order.getLines()) {
            long available = stockedMaterialRepository.sumQuantityByMaterialId(line.getMaterial().getId());
            if (available < line.getQuantity()) {
                throw new Exception("MATERIAL_ASSIGNMENT_ORDER_INSUFFICIENT_STOCK");
            }
        }

        for (MaterialAssignmentOrderLine line : order.getLines()) {
            debitMaterial(line.getMaterial().getId(), line.getQuantity());
        }

        order.setStatus(EStockAssignmentOrderStatus.ASSIGNED);
        order.setAssignedAt(LocalDateTime.now());
        if (StringUtils.hasText(operatorUserQrCode)) {
            String qr = operatorUserQrCode.trim();
            order.setAssignedByUserQr(qr);
            String fullName = usersService.resolveFullNameByQrCode(qr);
            if (StringUtils.hasText(fullName)) {
                order.setAssignedByFullName(fullName);
            }
        }
        return materialAssignmentOrderMapperService.mapToTO(materialAssignmentOrderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public String getAssignmentOrderCodeForWorkOrder(long workOrderId) throws Exception {
        return materialAssignmentOrderRepository.findFirstByWorkOrder_IdOrderByIdDesc(workOrderId)
                .map(MaterialAssignmentOrder::getCode)
                .orElseThrow(() -> new Exception("MATERIAL_ASSIGNMENT_ORDER_NOT_FOUND"));
    }

    private void debitMaterial(long materialId, int quantity) {
        int remaining = quantity;
        List<StockedMaterial> stocks = stockedMaterialRepository
                .findByMaterial_IdAndQuantityGreaterThanOrderByQuantityDesc(materialId, 0);
        for (StockedMaterial stocked : stocks) {
            if (remaining <= 0) {
                break;
            }
            int take = Math.min(remaining, stocked.getQuantity());
            stocked.setQuantity(stocked.getQuantity() - take);
            remaining -= take;
            stockedMaterialRepository.save(stocked);
        }
    }

    private static int toIssueQuantity(double requiredQuantity) {
        if (requiredQuantity <= 0) {
            return 0;
        }
        return (int) Math.min(Integer.MAX_VALUE, Math.ceil(requiredQuantity));
    }

    private java.util.Optional<MaterialAssignmentOrder> findByNormalizedCode(String code) throws Exception {
        String normalized = AssignmentOrderCodes.normalize(code);
        if (normalized == null) {
            throw new Exception("MATERIAL_ASSIGNMENT_ORDER_INVALID_CODE");
        }
        return materialAssignmentOrderRepository.findByCode(normalized);
    }

    private String generateUniqueEightDigitCode() {
        for (int attempt = 0; attempt < 200; attempt++) {
            int value = ThreadLocalRandom.current().nextInt(100_000_000);
            String code = String.format("%08d", value);
            if (!materialAssignmentOrderRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Could not generate unique material assignment order code");
    }
}
