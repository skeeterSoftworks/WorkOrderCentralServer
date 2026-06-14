package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMaterialOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialOrderTO {
    private Long id;
    /** Server-generated material order number (NM + timestamp). */
    private String code;
    /** Sum of line quantities; kept for list views and legacy clients. */
    private Integer quantity;
    /** First line material id when exactly one line; omitted for multi-line orders. */
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
    /** Nominal diameter from first/only line material (0 = not defined). */
    private Float materialDiameter;
    /** Nominal weight from first/only line material (0 = not defined). */
    private Float materialWeight;
    /** Nominal length from first/only line material (0 = not defined). */
    private Float materialLength;
    /** Nominal width from first/only line material (0 = not defined). */
    private Float materialWidth;
    private List<MaterialOrderLineTO> lines = new ArrayList<>();
}
