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
    private Integer quantity;
    private Long materialId;
    private String materialName;
    private String materialCode;
    private Long materialProviderId;
    private String materialProviderName;
    private EMaterialOrderStatus status;
    private LocalDateTime lastChanged;
    /** Raw Base64 or data URL for upload; omitted in list payloads. */
    private String certificateBase64;
    private Boolean certificatePresent;
}

