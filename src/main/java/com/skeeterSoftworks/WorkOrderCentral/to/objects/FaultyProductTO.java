package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaultyProductTO {
    private Long id;
    private String rejectReason;
    private String rejectCause;
    private String rejectComment;
    private LocalDateTime createdAt;
}
