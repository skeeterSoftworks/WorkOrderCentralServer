package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EEmailTemplateCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialOrderEmailRenderTO {
    private EEmailTemplateCode code;
    private Long materialOrderId;
}
