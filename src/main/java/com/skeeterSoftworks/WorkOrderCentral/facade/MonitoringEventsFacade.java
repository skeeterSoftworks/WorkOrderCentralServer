package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.to.objects.MonitoringClientEventTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/monitoring")
@CrossOrigin(origins = "*")
public class MonitoringEventsFacade {

    private final SimpMessagingTemplate template;

    public MonitoringEventsFacade(SimpMessagingTemplate template) {
        this.template = template;
    }

    @PostMapping("/events")
    public ResponseEntity<?> publishEvent(@RequestBody MonitoringClientEventTO body) {
        if (body == null || body.getEventType() == null || body.getEventType().isBlank()) {
            return ResponseEntity.badRequest().body("EVENT_TYPE_REQUIRED");
        }
        if (body.getTimestamp() == null) {
            body.setTimestamp(LocalDateTime.now());
        }
        template.convertAndSend("/websocket/monitoring-events", body);
        return ResponseEntity.ok().build();
    }
}
