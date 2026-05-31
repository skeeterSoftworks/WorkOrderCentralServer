package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMaterialOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialOrderTO {
    private Long id;
    /** Server-generated material order number (NM + timestamp). */
    private String code;
    private Integer quantity;
    private Long materialId;
    private String materialName;
    private String materialCode;
    private Long materialProviderId;
    private String materialProviderName;
    private EMaterialOrderStatus status;
    private LocalDateTime lastChanged;
    private LocalDateTime createdAt;
    private LocalDateTime rejectedAt;
    /** Raw Base64 or data URL for upload; omitted in list payloads. */
    private String certificateBase64;
    private Boolean certificatePresent;
}

