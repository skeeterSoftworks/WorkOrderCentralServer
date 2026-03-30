package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MachineTO {
    private Long id;
    private String machineName;
    private String manufacturer;
    private Integer manufactureYear;
    private String internalNumber;
    private String serialNumber;
    private String location;
}
