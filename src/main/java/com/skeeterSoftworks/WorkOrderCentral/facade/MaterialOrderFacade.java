package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import com.skeeterSoftworks.WorkOrderCentral.service.MaterialOrderSearchCriteria;
import com.skeeterSoftworks.WorkOrderCentral.service.MaterialOrderService;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMaterialOrderStatus;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderStatusTransitionTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderCertificateTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderPageTO;
import com.skeeterSoftworks.WorkOrderCentral.util.MaterialOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/material-orders")
@CrossOrigin(origins = "*")
public class MaterialOrderFacade {

    private final MaterialOrderService materialOrderService;

    public MaterialOrderFacade(MaterialOrderService materialOrderService) {
        this.materialOrderService = materialOrderService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            List<MaterialOrder> all = materialOrderService.getAllMaterialOrders();
            return ResponseEntity.ok(all.stream().map(this::toTO).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_MATERIAL_ORDERS");
        }
    }

    @Transactional(readOnly = true)
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean asc,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String materialName,
            @RequestParam(required = false) String materialProviderName,
            @RequestParam(required = false) Integer quantity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate lastChangedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate lastChangedTo,
            @RequestParam(required = false) Boolean certificatePresent) {
        try {
            EMaterialOrderStatus statusEnum = null;
            if (StringUtils.hasText(status) && !"ALL".equalsIgnoreCase(status.trim())) {
                statusEnum = EMaterialOrderStatus.valueOf(status.trim());
            }
            MaterialOrderSearchCriteria criteria = MaterialOrderSearchCriteria.builder()
                    .status(statusEnum)
                    .createdFrom(createdFrom)
                    .createdTo(createdTo)
                    .code(code)
                    .materialName(materialName)
                    .materialProviderName(materialProviderName)
                    .quantity(quantity)
                    .lastChangedFrom(lastChangedFrom)
                    .lastChangedTo(lastChangedTo)
                    .certificatePresent(certificatePresent)
                    .build();
            Page<MaterialOrder> result = materialOrderService.searchMaterialOrders(
                    criteria, page, size, sortBy, asc);
            MaterialOrderPageTO pageTo = new MaterialOrderPageTO(
                    result.getContent().stream().map(this::toTO).toList(),
                    result.getTotalElements(),
                    result.getNumber(),
                    result.getSize());
            return ResponseEntity.ok(pageTo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("MATERIAL_ORDER_SEARCH_INVALID_STATUS");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_MATERIAL_ORDERS");
        }
    }

    @GetMapping("/open-for-reception")
    public ResponseEntity<?> getOpenForReception() {
        try {
            List<MaterialOrder> list = materialOrderService.getOpenForReception();
            return ResponseEntity.ok(list.stream().map(this::toTO).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_MATERIAL_ORDERS_OPEN_FOR_RECEPTION");
        }
    }

    @GetMapping("/stale-monitoring")
    public ResponseEntity<?> getStaleMonitoring() {
        try {
            List<MaterialOrder> list = materialOrderService.findStaleForMonitoring();
            return ResponseEntity.ok(list.stream().map(this::toTO).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_STALE_MATERIAL_ORDERS");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody MaterialOrderTO to) {
        try {
            normalizeLegacyCreatePayload(to);
            MaterialOrder saved = materialOrderService.addMaterialOrder(toEntity(to), to.getLines());
            return ResponseEntity.ok(toTO(saved));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/transition-status")
    public ResponseEntity<?> transitionStatus(
            @PathVariable Long id,
            @RequestBody MaterialOrderStatusTransitionTO body) {
        try {
            EMaterialOrderStatus next = body != null ? body.getStatus() : null;
            MaterialOrder saved = materialOrderService.transitionStatus(id, next);
            return ResponseEntity.ok(toTO(saved));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        try {
            MaterialOrder saved = materialOrderService.rejectMaterialOrder(id);
            return ResponseEntity.ok(toTO(saved));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/certificate")
    public ResponseEntity<?> getCertificate(@PathVariable Long id) {
        try {
            String dataUrl = materialOrderService.getCertificateDataUrl(id);
            return ResponseEntity.ok(new MaterialOrderCertificateTO(dataUrl));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if ("MATERIAL_ORDER_NOT_FOUND".equals(e.getMessage())
                    || "MATERIAL_ORDER_CERTIFICATE_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/certificate")
    public ResponseEntity<?> uploadCertificate(
            @PathVariable Long id,
            @RequestBody MaterialOrderCertificateTO body) {
        try {
            String payload = body != null ? body.getCertificateBase64() : null;
            MaterialOrder saved = materialOrderService.uploadCertificate(id, payload);
            MaterialOrderTO to = toTO(saved);
            to.setCertificatePresent(true);
            return ResponseEntity.ok(to);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private MaterialOrderTO toTO(MaterialOrder e) {
        Set<Long> receivedLineIds = e.getId() > 0
                ? materialOrderService.findReceivedLineIds(e.getId())
                : new HashSet<>();
        return MaterialOrderMapper.toTO(e, receivedLineIds);
    }

    private MaterialOrder toEntity(MaterialOrderTO to) {
        MaterialOrder e = new MaterialOrder();
        if (to.getId() != null) {
            e.setId(to.getId());
        }
        MaterialProvider p = new MaterialProvider();
        p.setId(to.getMaterialProviderId());
        e.setMaterialProvider(p);
        e.setStatus(null);
        e.setCertificate(null);
        return e;
    }

    private static void normalizeLegacyCreatePayload(MaterialOrderTO to) {
        if (to == null) {
            return;
        }
        if (to.getLines() != null && !to.getLines().isEmpty()) {
            return;
        }
        if (to.getMaterialId() != null && to.getQuantity() != null && to.getQuantity() > 0) {
            com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderLineTO line =
                    new com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderLineTO();
            line.setMaterialId(to.getMaterialId());
            line.setQuantity(to.getQuantity());
            to.setLines(java.util.List.of(line));
        }
    }
}
