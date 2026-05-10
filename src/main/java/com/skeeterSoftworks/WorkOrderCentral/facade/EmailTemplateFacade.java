package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.service.EmailTemplateService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.EmailTemplateTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderEmailRenderTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.RenderedEmailTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/email-templates")
@CrossOrigin(origins = "*")
public class EmailTemplateFacade {

    private final EmailTemplateService emailTemplateService;

    public EmailTemplateFacade(EmailTemplateService emailTemplateService) {
        this.emailTemplateService = emailTemplateService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(emailTemplateService.getAll());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_EMAIL_TEMPLATES");
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody EmailTemplateTO body) {
        try {
            return ResponseEntity.ok(emailTemplateService.save(body));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/render-material-order")
    public ResponseEntity<?> renderMaterialOrder(@RequestBody MaterialOrderEmailRenderTO body) {
        try {
            if (body == null || body.getCode() == null || body.getMaterialOrderId() == null) {
                return ResponseEntity.badRequest().body("EMAIL_RENDER_REQUEST_INVALID");
            }
            RenderedEmailTO rendered =
                    emailTemplateService.renderForMaterialOrder(body.getCode(), body.getMaterialOrderId());
            return ResponseEntity.ok(rendered);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
