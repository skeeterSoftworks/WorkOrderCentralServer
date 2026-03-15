package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Machine;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Tool;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MachineRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ToolRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductMapperService {

    private final MachineRepository machineRepository;
    private final ToolRepository toolRepository;

    @Autowired
    public ProductMapperService(MachineRepository machineRepository, ToolRepository toolRepository) {
        this.machineRepository = machineRepository;
        this.toolRepository = toolRepository;
    }

    public ProductTO mapToTO(Product product) {
        if (product == null) return null;
        ProductTO to = new ProductTO();
        to.setId(product.getId());
        to.setName(product.getName());
        to.setDescription(product.getDescription());
        if (product.getMachines() != null && !product.getMachines().isEmpty()) {
            to.setMachineIds(product.getMachines().stream().map(Machine::getId).collect(Collectors.toList()));
        } else {
            to.setMachineIds(Collections.emptyList());
        }
        if (product.getTool() != null) to.setToolId(product.getTool().getId());
        return to;
    }

    public Product mapToEntity(ProductTO to) {
        if (to == null) return null;
        Product product = new Product();
        if (to.getId() != null) {
            product.setId(to.getId());
        }
        product.setName(to.getName());
        product.setDescription(to.getDescription());
        if (to.getMachineIds() != null && !to.getMachineIds().isEmpty()) {
            List<Machine> machineList = new ArrayList<>();
            for (Long id : to.getMachineIds()) {
                machineRepository.findById(id).ifPresent(machineList::add);
            }
            product.setMachines(machineList);
        } else {
            product.setMachines(new ArrayList<>());
        }
        if (to.getToolId() != null) {
            toolRepository.findById(to.getToolId()).ifPresent(product::setTool);
        }
        return product;
    }
}

