package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderLine;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.DeliveryNoteRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialOrderReceptionRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialProviderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMaterialOrderStatus;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EUnitOfMeasure;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderLineTO;
import com.skeeterSoftworks.WorkOrderCentral.util.BinaryMediaEncodingUtils;
import com.skeeterSoftworks.WorkOrderCentral.util.MaterialOrderCodeGenerator;
import com.skeeterSoftworks.WorkOrderCentral.util.DeliveryNoteMapper;
import com.skeeterSoftworks.WorkOrderCentral.util.MaterialOrderMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class MaterialOrderService {

    public static final int STALE_LAST_CHANGE_DAYS = 5;

    private static final Set<EMaterialOrderStatus> STALE_MONITOR_EXCLUDED_STATUSES = EnumSet.of(
            EMaterialOrderStatus.RECEIVED_IN_STOCK,
            EMaterialOrderStatus.VALIDATED,
            EMaterialOrderStatus.REJECTED);

    private static final Set<EMaterialOrderStatus> REJECT_BLOCKED_STATUSES = EnumSet.of(
            EMaterialOrderStatus.RECEIVED_IN_STOCK,
            EMaterialOrderStatus.VALIDATED,
            EMaterialOrderStatus.REJECTED);

    private static final Set<EMaterialOrderStatus> MANUAL_TRANSITION_TARGETS = EnumSet.of(
            EMaterialOrderStatus.ORDER_SENT,
            EMaterialOrderStatus.ORDER_ACKNOWLEDGED,
            EMaterialOrderStatus.ORDER_ACCEPTED,
            EMaterialOrderStatus.IN_TRANSPORT);

    private final MaterialOrderRepository materialOrderRepository;
    private final MaterialRepository materialRepository;
    private final MaterialProviderRepository materialProviderRepository;
    private final MaterialOrderReceptionRepository materialOrderReceptionRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;

    public MaterialOrderService(
            MaterialOrderRepository materialOrderRepository,
            MaterialRepository materialRepository,
            MaterialProviderRepository materialProviderRepository,
            MaterialOrderReceptionRepository materialOrderReceptionRepository,
            DeliveryNoteRepository deliveryNoteRepository
    ) {
        this.materialOrderRepository = materialOrderRepository;
        this.materialRepository = materialRepository;
        this.materialProviderRepository = materialProviderRepository;
        this.materialOrderReceptionRepository = materialOrderReceptionRepository;
        this.deliveryNoteRepository = deliveryNoteRepository;
    }

    public List<MaterialOrder> getAllMaterialOrders() {
        return materialOrderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<MaterialOrder> searchMaterialOrders(MaterialOrderSearchCriteria criteria, int page,
            int size, String sortBy, boolean asc) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 200);
        Specification<MaterialOrder> spec = MaterialOrderSearchSpecifications.from(criteria);
        Pageable pageable = PageRequest.of(safePage, safeSize, buildSort(sortBy, asc));
        return materialOrderRepository.findAll(spec, pageable);
    }

    private static Sort buildSort(String sortBy, boolean asc) {
        Sort.Direction direction = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        String field = StringUtils.hasText(sortBy) ? sortBy.trim() : "createdAt";
        return switch (field) {
            case "materialProviderName" -> Sort.by(new Sort.Order(direction, "materialProvider.name"));
            case "certificatePresent" -> Sort.by(new Sort.Order(direction, "certificatePresent"));
            case "code", "status", "lastChanged", "createdAt" ->
                    Sort.by(new Sort.Order(direction, field));
            case "materialName", "quantity" -> Sort.by(new Sort.Order(direction, "createdAt"));
            default -> Sort.by(new Sort.Order(direction, "createdAt"));
        };
    }

    public Optional<MaterialOrder> getMaterialOrderById(Long id) {
        return materialOrderRepository.findById(id);
    }

    @Transactional
    public MaterialOrder addMaterialOrder(MaterialOrder order, List<MaterialOrderLineTO> lineInputs) throws Exception {
        order.setId(0);
        order.setStatus(EMaterialOrderStatus.ORDER_CREATED);
        LocalDateTime now = LocalDateTime.now();
        order.setLastChanged(now);
        order.setCreatedAt(now);
        order.setCertificate(null);
        order.setCode(null);
        order.setLines(new ArrayList<>());
        validateProvider(order);
        validateAndAttachLines(order, lineInputs);
        order.setCode(MaterialOrderCodeGenerator.resolveUnique(now, materialOrderRepository::existsByCode));
        return materialOrderRepository.save(order);
    }

    public List<MaterialOrder> findStaleForMonitoring() {
        LocalDateTime threshold = LocalDateTime.now().minus(STALE_LAST_CHANGE_DAYS, ChronoUnit.DAYS);
        return materialOrderRepository.findStaleMonitoringCandidates(threshold, STALE_MONITOR_EXCLUDED_STATUSES);
    }

    public List<MaterialOrder> getOpenForReception() {
        return materialOrderRepository.findByStatus(EMaterialOrderStatus.IN_TRANSPORT).stream()
                .filter(this::hasUnreceivedLine)
                .toList();
    }

    @Transactional
    public MaterialOrder transitionStatus(Long id, EMaterialOrderStatus newStatus) throws Exception {
        if (newStatus == null || !MANUAL_TRANSITION_TARGETS.contains(newStatus)) {
            throw new Exception("MATERIAL_ORDER_STATUS_TRANSITION_NOT_ALLOWED");
        }
        MaterialOrder order = materialOrderRepository.findById(id).orElseThrow(() -> new Exception("MATERIAL_ORDER_NOT_FOUND"));
        if (order.getStatus() == EMaterialOrderStatus.RECEIVED_IN_STOCK
                || order.getStatus() == EMaterialOrderStatus.VALIDATED
                || order.getStatus() == EMaterialOrderStatus.REJECTED) {
            throw new Exception("MATERIAL_ORDER_STATUS_LOCKED");
        }
        order.setStatus(newStatus);
        order.setLastChanged(LocalDateTime.now());
        return materialOrderRepository.save(order);
    }

    @Transactional
    public MaterialOrder rejectMaterialOrder(Long id) throws Exception {
        MaterialOrder order = materialOrderRepository.findById(id).orElseThrow(() -> new Exception("MATERIAL_ORDER_NOT_FOUND"));
        if (REJECT_BLOCKED_STATUSES.contains(order.getStatus())) {
            throw new Exception("MATERIAL_ORDER_REJECT_NOT_ALLOWED");
        }
        if (deliveryNoteRepository.existsByMaterialOrder_Id(id)) {
            throw new Exception("MATERIAL_ORDER_HAS_RECEPTION");
        }
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(EMaterialOrderStatus.REJECTED);
        order.setRejectedAt(now);
        order.setLastChanged(now);
        return materialOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public String getCertificateDataUrl(Long id) throws Exception {
        MaterialOrder order = materialOrderRepository.findById(id).orElseThrow(() -> new Exception("MATERIAL_ORDER_NOT_FOUND"));
        if (order.getCertificate() == null || order.getCertificate().length == 0) {
            throw new Exception("MATERIAL_ORDER_CERTIFICATE_NOT_FOUND");
        }
        return BinaryMediaEncodingUtils.encodeToDataUrl(order.getCertificate());
    }

    @Transactional
    public MaterialOrder uploadCertificate(Long id, String certificateBase64) throws Exception {
        MaterialOrder order = materialOrderRepository.findById(id).orElseThrow(() -> new Exception("MATERIAL_ORDER_NOT_FOUND"));
        if (order.getStatus() == EMaterialOrderStatus.REJECTED) {
            throw new Exception("MATERIAL_ORDER_CERTIFICATE_UPLOAD_NOT_ALLOWED");
        }
        byte[] bytes = BinaryMediaEncodingUtils.decodeBase64Payload(certificateBase64);
        BinaryMediaEncodingUtils.validateCertificateSize(bytes);
        order.setCertificate(bytes);
        order.setLastChanged(LocalDateTime.now());
        return materialOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Set<Long> findReceivedLineIds(Long materialOrderId) {
        return deliveryNoteRepository.findFullyReceivedLineIdsByMaterialOrderId(materialOrderId);
    }

    @Transactional(readOnly = true)
    public Map<Long, MaterialOrderMapper.LineDeliverySummary> buildLineDeliverySummaries(MaterialOrder order) {
        Map<Long, MaterialOrderMapper.LineDeliverySummary> map = new HashMap<>();
        if (order.getLines() == null) {
            return map;
        }
        for (MaterialOrderLine line : order.getLines()) {
            if (line.getId() <= 0) {
                continue;
            }
            int receivedTotal = deliveryNoteRepository.sumQuantityByMaterialOrderLineId(line.getId());
            var notes = deliveryNoteRepository.findByMaterialOrderLine_IdOrderByReceivedAtDescIdDesc(line.getId())
                    .stream()
                    .map(DeliveryNoteMapper::toTO)
                    .toList();
            map.put(line.getId(), new MaterialOrderMapper.LineDeliverySummary(receivedTotal, notes));
        }
        return map;
    }

    private void validateProvider(MaterialOrder order) throws Exception {
        if (order.getMaterialProvider() == null
                || order.getMaterialProvider().getId() == null
                || order.getMaterialProvider().getId() <= 0) {
            throw new Exception("MATERIAL_ORDER_PROVIDER_REQUIRED");
        }
        MaterialProvider provider = materialProviderRepository.findById(order.getMaterialProvider().getId()).orElse(null);
        if (provider == null) {
            throw new Exception("MATERIAL_PROVIDER_NOT_FOUND");
        }
        order.setMaterialProvider(provider);
    }

    private void validateAndAttachLines(MaterialOrder order, List<MaterialOrderLineTO> lineInputs) throws Exception {
        if (lineInputs == null || lineInputs.isEmpty()) {
            throw new Exception("MATERIAL_ORDER_LINES_REQUIRED");
        }
        if (!MaterialOrderMapper.duplicateMaterialIds(lineInputs).isEmpty()) {
            throw new Exception("MATERIAL_ORDER_DUPLICATE_MATERIAL");
        }
        MaterialProvider provider = order.getMaterialProvider();
        Set<Long> usedMaterialIds = new HashSet<>();
        for (MaterialOrderLineTO input : lineInputs) {
            if (input.getMaterialId() == null || input.getMaterialId() <= 0) {
                throw new Exception("MATERIAL_ORDER_MATERIAL_REQUIRED");
            }
            if (input.getQuantity() == null || input.getQuantity() <= 0) {
                throw new Exception("MATERIAL_ORDER_INVALID_QUANTITY");
            }
            if (!usedMaterialIds.add(input.getMaterialId())) {
                throw new Exception("MATERIAL_ORDER_DUPLICATE_MATERIAL");
            }
            Material material = materialRepository.findById(input.getMaterialId()).orElse(null);
            if (material == null) {
                throw new Exception("MATERIAL_NOT_FOUND");
            }
            boolean providerAttachedToMaterial = material.getProviders() != null
                    && material.getProviders().stream()
                    .anyMatch(p -> p.getId() != null && p.getId().equals(provider.getId()));
            if (!providerAttachedToMaterial) {
                throw new Exception("MATERIAL_ORDER_PROVIDER_NOT_ALLOWED_FOR_MATERIAL");
            }
            MaterialOrderLine line = new MaterialOrderLine();
            line.setMaterialOrder(order);
            line.setMaterial(material);
            line.setQuantity(input.getQuantity());
            line.setUnitOfMeasure(input.getMaterialUnitOfMeasure() != null
                    ? input.getMaterialUnitOfMeasure()
                    : EUnitOfMeasure.PCS);
            order.getLines().add(line);
        }
    }

    private boolean hasUnreceivedLine(MaterialOrder order) {
        if (order.getLines() == null || order.getLines().isEmpty()) {
            return false;
        }
        Set<Long> received = deliveryNoteRepository.findFullyReceivedLineIdsByMaterialOrderId(order.getId());
        return order.getLines().stream().anyMatch(line -> line.getId() > 0 && !received.contains(line.getId()));
    }
}
