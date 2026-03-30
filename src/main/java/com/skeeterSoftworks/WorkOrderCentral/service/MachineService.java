package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Machine;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MachineService {

    private final MachineRepository machineRepository;

    @Autowired
    public MachineService(MachineRepository machineRepository) {
        this.machineRepository = machineRepository;
    }

    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

    public Optional<Machine> getMachineById(Long id) {
        return machineRepository.findById(id);
    }

    public Machine addMachine(Machine machine) {
        machine.setId(null);
        return machineRepository.save(machine);
    }

    public Machine updateMachine(Machine machine) throws Exception {
        if (machine.getId() == null || machine.getId() <= 0 || !machineRepository.existsById(machine.getId())) {
            throw new Exception("MACHINE_NOT_FOUND");
        }
        return machineRepository.save(machine);
    }

    public void deleteMachine(Long id) throws Exception {
        if (!machineRepository.existsById(id)) {
            throw new Exception("MACHINE_NOT_FOUND");
        }
        machineRepository.deleteById(id);
    }
}
