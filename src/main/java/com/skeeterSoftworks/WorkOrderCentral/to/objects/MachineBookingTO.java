package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMachineBookingStatus;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMachineBookingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MachineBookingTO {

    private Long id;
    private Long machineId;
    /** Denormalized for clients (e.g. work order details). */
    private String machineName;
    private Long workOrderId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private EMachineBookingType type;
    private EMachineBookingStatus status;
    private String comment;
}

