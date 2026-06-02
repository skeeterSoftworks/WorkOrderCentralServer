package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialOrderCertificateTO {
    /** Data URL or raw Base64 of the certificate file (PDF or image). */
    private String certificateBase64;
}
