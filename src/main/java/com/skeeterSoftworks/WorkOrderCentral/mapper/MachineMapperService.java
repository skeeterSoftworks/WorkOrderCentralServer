package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Machine;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MachineTO;
import org.springframework.stereotype.Service;

import java.util.Base64;

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
        if (machine.getMachineImage() != null && machine.getMachineImage().length > 0) {
            to.setMachineImageBase64(Base64.getEncoder().encodeToString(machine.getMachineImage()));
        }
        return to;
    }

    /**
     * Maps scalar fields and, when {@code machineImageBase64} is non-null in the TO, updates or clears the image.
     * When {@code machineImageBase64} is null, does not call {@link Machine#setMachineImage(byte[])} (caller may merge from DB).
     */
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
        if (to.getMachineImageBase64() != null) {
            if (to.getMachineImageBase64().isBlank()) {
                machine.setMachineImage(null);
            } else {
                machine.setMachineImage(decodeBase64Image(to.getMachineImageBase64()));
            }
        }
        return machine;
    }

    private static byte[] decodeBase64Image(String b64) {
        if (b64 == null || b64.isBlank()) {
            return null;
        }
        String s = b64.trim();
        int comma = s.indexOf(',');
        if (s.startsWith("data:") && comma > 0) {
            s = s.substring(comma + 1);
        }
        return Base64.getDecoder().decode(s);
    }
}
