package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialOrderReceptionRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialProviderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMaterialOrderStatus;
import com.skeeterSoftworks.WorkOrderCentral.util.BinaryMediaEncodingUtils;
import com.skeeterSoftworks.WorkOrderCentral.util.MaterialOrderCodeGenerator;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class MaterialOrderService {

    public static final int STALE_LAST_CHANGE_DAYS = 3;

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

    public MaterialOrderService(
            MaterialOrderRepository materialOrderRepository,
            MaterialRepository materialRepository,
            MaterialProviderRepository materialProviderRepository,
            MaterialOrderReceptionRepository materialOrderReceptionRepository
    ) {
        this.materialOrderRepository = materialOrderRepository;
        this.materialRepository = materialRepository;
        this.materialProviderRepository = materialProviderRepository;
        this.materialOrderReceptionRepository = materialOrderReceptionRepository;
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
            case "materialName" -> Sort.by(new Sort.Order(direction, "material.name"));
            case "materialProviderName" -> Sort.by(new Sort.Order(direction, "materialProvider.name"));
            case "certificatePresent" -> Sort.by(new Sort.Order(direction, "certificatePresent"));
            case "code", "quantity", "status", "lastChanged", "createdAt" ->
                    Sort.by(new Sort.Order(direction, field));
            default -> Sort.by(new Sort.Order(direction, "createdAt"));
        };
    }

    public Optional<MaterialOrder> getMaterialOrderById(Long id) {
        return materialOrderRepository.findById(id);
    }

    public MaterialOrder addMaterialOrder(MaterialOrder order) throws Exception {
        order.setId(0);
        // Creation flow owns initial state; clients must not set this.
        order.setStatus(EMaterialOrderStatus.ORDER_CREATED);
        LocalDateTime now = LocalDateTime.now();
        order.setLastChanged(now);
        order.setCreatedAt(now);
        order.setCertificate(null);
        order.setCode(null);
        validate(order);
        order.setCode(MaterialOrderCodeGenerator.resolveUnique(now, materialOrderRepository::existsByCode));
        return materialOrderRepository.save(order);
    }

    public List<MaterialOrder> findStaleForMonitoring() {
        LocalDateTime threshold = LocalDateTime.now().minus(STALE_LAST_CHANGE_DAYS, ChronoUnit.DAYS);
        return materialOrderRepository.findStaleMonitoringCandidates(threshold, STALE_MONITOR_EXCLUDED_STATUSES);
    }

    public List<MaterialOrder> getOpenForReception() {
        return materialOrderRepository.findByStatus(EMaterialOrderStatus.IN_TRANSPORT);
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
        if (materialOrderReceptionRepository.existsByMaterialOrder_Id(id)) {
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

