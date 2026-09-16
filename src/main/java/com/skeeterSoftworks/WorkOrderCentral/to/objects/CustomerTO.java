package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CustomerTO {
    private Long id;
    private String companyName;
    private String buyerId;
    private String contactPerson;
    private String emailAddress;
    private String phoneNumber;
    private String addressData;
    private String description;
}

