package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EEmailTemplateCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateTO {
    private EEmailTemplateCode code;
    private String subjectTemplate;
    private String bodyTemplate;
}
