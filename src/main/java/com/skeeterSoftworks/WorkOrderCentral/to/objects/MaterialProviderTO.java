package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialProviderTO {
    private Long id;
    private String name;
    private String contactPerson;
    private String emailAddress;
    private String phoneNumber;
    private Integer grade;
}
