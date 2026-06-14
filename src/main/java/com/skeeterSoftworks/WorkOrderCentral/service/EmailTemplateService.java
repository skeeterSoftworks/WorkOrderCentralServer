package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.EmailTemplate;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.EmailTemplateRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EEmailTemplateCode;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.EmailTemplateTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.RenderedEmailTO;
import com.skeeterSoftworks.WorkOrderCentral.util.MaterialOrderMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final MaterialOrderRepository materialOrderRepository;

    public EmailTemplateService(
            EmailTemplateRepository emailTemplateRepository,
            MaterialOrderRepository materialOrderRepository) {
        this.emailTemplateRepository = emailTemplateRepository;
        this.materialOrderRepository = materialOrderRepository;
    }

    @PostConstruct
    public void seedDefaultsIfMissing() {
        for (EEmailTemplateCode code : EEmailTemplateCode.values()) {
            if (emailTemplateRepository.findByCode(code).isEmpty()) {
                EmailTemplate row = new EmailTemplate();
                row.setCode(code);
                row.setSubjectTemplate(defaultSubject(code));
                row.setBodyTemplate(defaultBody(code));
                emailTemplateRepository.save(row);
            }
        }
    }

    public List<EmailTemplateTO> getAll() {
        return emailTemplateRepository.findAllByOrderByCodeAsc().stream().map(this::toTO).toList();
    }

    @Transactional
    public EmailTemplateTO save(EmailTemplateTO to) throws Exception {
        if (to == null || to.getCode() == null) {
            throw new Exception("EMAIL_TEMPLATE_CODE_REQUIRED");
        }
        EmailTemplate row = emailTemplateRepository
                .findByCode(to.getCode())
                .orElseThrow(() -> new Exception("EMAIL_TEMPLATE_NOT_FOUND"));
        if (to.getSubjectTemplate() == null || to.getSubjectTemplate().isBlank()) {
            throw new Exception("EMAIL_TEMPLATE_SUBJECT_REQUIRED");
        }
        if (to.getBodyTemplate() == null || to.getBodyTemplate().isBlank()) {
            throw new Exception("EMAIL_TEMPLATE_BODY_REQUIRED");
        }
        row.setSubjectTemplate(to.getSubjectTemplate().trim());
        row.setBodyTemplate(to.getBodyTemplate().trim());
        return toTO(emailTemplateRepository.save(row));
    }

    public RenderedEmailTO renderForMaterialOrder(EEmailTemplateCode code, long materialOrderId) throws Exception {
        EmailTemplate template = emailTemplateRepository
                .findByCode(code)
                .orElseThrow(() -> new Exception("EMAIL_TEMPLATE_NOT_FOUND"));
        MaterialOrder order = materialOrderRepository
                .findById(materialOrderId)
                .orElseThrow(() -> new Exception("MATERIAL_ORDER_NOT_FOUND"));
        Map<String, String> vars = placeholdersFor(order);
        return new RenderedEmailTO(
                applyPlaceholders(template.getSubjectTemplate(), vars),
                applyPlaceholders(template.getBodyTemplate(), vars));
    }

    private Map<String, String> placeholdersFor(MaterialOrder order) {
        Map<String, String> m = new LinkedHashMap<>();
        String linesSummary = MaterialOrderMapper.linesSummary(order);
        String materialName = "";
        String materialCode = "";
        if (order.getLines() != null && order.getLines().size() == 1 && order.getLines().get(0).getMaterial() != null) {
            Material material = order.getLines().get(0).getMaterial();
            materialName = material.getName() != null ? material.getName() : "";
            materialCode = material.getCode() != null ? material.getCode() : "";
        }
        String materialLabel = !linesSummary.isBlank()
                ? linesSummary
                : (!materialName.isBlank() ? materialName : (!materialCode.isBlank() ? materialCode : ""));
        String providerName = order.getMaterialProvider() != null && order.getMaterialProvider().getName() != null
                ? order.getMaterialProvider().getName()
                : "";
        String contactPerson =
                order.getMaterialProvider() != null && order.getMaterialProvider().getContactPerson() != null
                        ? order.getMaterialProvider().getContactPerson()
                        : "";
        String providerLabel = !providerName.isBlank() ? providerName : contactPerson;
        m.put("providerName", providerLabel);
        m.put("providerCompany", providerName);
        m.put("providerContact", contactPerson);
        m.put("materialLabel", materialLabel);
        m.put("materialLinesSummary", linesSummary);
        m.put("materialName", materialName);
        m.put("materialCode", materialCode);
        String materialOrderCode = order.getCode() != null ? order.getCode() : "";
        m.put("materialOrderCode", materialOrderCode);
        int totalQuantity = order.getLines() != null
                ? order.getLines().stream().mapToInt(line -> line.getQuantity()).sum()
                : 0;
        m.put("quantity", String.valueOf(totalQuantity));
        return m;
    }

    static String applyPlaceholders(String template, Map<String, String> vars) {
        if (template == null) {
            return "";
        }
        String out = template;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            String val = e.getValue() != null ? e.getValue() : "";
            out = out.replace("{{" + e.getKey() + "}}", val);
        }
        return out;
    }

    private EmailTemplateTO toTO(EmailTemplate e) {
        return new EmailTemplateTO(e.getCode(), e.getSubjectTemplate(), e.getBodyTemplate());
    }

    private static String defaultSubject(EEmailTemplateCode code) {
        return switch (code) {
            case MATERIAL_ORDER_INQUIRY -> "Material order {{materialOrderCode}} — {{materialLabel}}";
            case MATERIAL_ORDER_REMINDER -> "Reminder: material order {{materialOrderCode}}";
            case MATERIAL_DELIVERY_LATE -> "Late delivery: material order {{materialOrderCode}}";
        };
    }

    private static String defaultBody(EEmailTemplateCode code) {
        return switch (code) {
            case MATERIAL_ORDER_INQUIRY ->
                    "Dear {{providerName}},\n\n"
                            + "Order number: {{materialOrderCode}}\n"
                            + "Material: {{materialLabel}}\n"
                            + "Quantity: {{quantity}}\n\n"
                            + "Please confirm this material order.";
            case MATERIAL_ORDER_REMINDER ->
                    "Dear {{providerName}},\n\n"
                            + "This is a friendly reminder regarding material order {{materialOrderCode}} for "
                            + "{{materialLabel}} (quantity {{quantity}}).\n\n"
                            + "Please confirm status at your earliest convenience.\n\n"
                            + "Thank you.";
            case MATERIAL_DELIVERY_LATE ->
                    "Dear {{providerName}},\n\n"
                            + "We are writing regarding material order {{materialOrderCode}} for {{materialLabel}} "
                            + "(quantity {{quantity}}). The delivery is currently delayed beyond our agreed timeline.\n\n"
                            + "Please advise on the revised delivery schedule.\n\n"
                            + "Thank you.";
        };
    }
}
