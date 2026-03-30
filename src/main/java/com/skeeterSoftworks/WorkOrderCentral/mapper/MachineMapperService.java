package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Machine;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MachineTO;
import org.springframework.stereotype.Service;

@Service
public class MachineMapperService {

    public MachineTO mapToTO(Machine machine) {
        if (machine == null) return null;
        MachineTO to = new MachineTO();
        to.setId(machine.getId());
        to.setMachineName(machine.getMachineName());
        to.setManufacturer(machine.getManufacturer());
        to.setManufactureYear(machine.getManufactureYear());
        to.setInternalNumber(machine.getInternalNumber());
        to.setSerialNumber(machine.getSerialNumber());
        to.setLocation(machine.getLocation());
        return to;
    }

    public Machine mapToEntity(MachineTO to) {
        if (to == null) return null;
        Machine machine = new Machine();
        if (to.getId() != null) {
            machine.setId(to.getId());
        }
        machine.setMachineName(to.getMachineName());
        machine.setManufacturer(to.getManufacturer());
        machine.setManufactureYear(to.getManufactureYear());
        machine.setInternalNumber(to.getInternalNumber());
        machine.setSerialNumber(to.getSerialNumber());
        machine.setLocation(to.getLocation());
        return machine;
    }
}
