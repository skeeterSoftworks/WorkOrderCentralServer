package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Machine;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MachineBookingRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MachineRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MachineService {

    private final MachineRepository machineRepository;
    private final ProductRepository productRepository;
    private final MachineBookingRepository machineBookingRepository;

    @Autowired
    public MachineService(
            MachineRepository machineRepository,
            ProductRepository productRepository,
            MachineBookingRepository machineBookingRepository) {
        this.machineRepository = machineRepository;
        this.productRepository = productRepository;
        this.machineBookingRepository = machineBookingRepository;
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

    /**
     * Updates only mutable scalar fields (and optionally image) on the managed entity so
     * {@link com.skeeterSoftworks.WorkOrderCentral.domain.objects.MachineBooking} and product links stay intact.
     */
    @Transactional
    public Machine updateMachineScalars(Long id, Machine scalarPatch, boolean updateImage) throws Exception {
        if (id == null || id <= 0 || !machineRepository.existsById(id)) {
            throw new Exception("MACHINE_NOT_FOUND");
        }
        Machine existing = machineRepository.findById(id).orElseThrow(() -> new Exception("MACHINE_NOT_FOUND"));
        existing.setMachineName(scalarPatch.getMachineName());
        existing.setManufacturer(scalarPatch.getManufacturer());
        existing.setManufactureYear(scalarPatch.getManufactureYear());
        existing.setInternalNumber(scalarPatch.getInternalNumber());
        existing.setSerialNumber(scalarPatch.getSerialNumber());
        existing.setLocation(scalarPatch.getLocation());
        if (updateImage) {
            existing.setMachineImage(scalarPatch.getMachineImage());
        }
        return machineRepository.save(existing);
    }

    public void deleteMachine(Long id) throws Exception {
        if (!machineRepository.existsById(id)) {
            throw new Exception("MACHINE_NOT_FOUND");
        }
        var linkedProducts = productRepository.findByMachines_Id(id);
        if (linkedProducts != null && !linkedProducts.isEmpty()) {
            throw new MachineDeleteBlockedException(linkedProducts.size());
        }
        long bookingCount = machineBookingRepository.countByMachine_Id(id);
        if (bookingCount > 0) {
            int c = bookingCount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) bookingCount;
            throw new MachineDeleteBlockedByBookingsException(c);
        }
        machineRepository.deleteById(id);
    }
}
