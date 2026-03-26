package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Machine;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MeasuringFeaturePrototype;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Tool;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MachineRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ToolRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MeasuringFeaturePrototypeTO;
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
        to.setReference(product.getReference());
        if (product.getMachines() != null && !product.getMachines().isEmpty()) {
            to.setMachineIds(product.getMachines().stream().map(Machine::getId).collect(Collectors.toList()));
        } else {
            to.setMachineIds(Collections.emptyList());
        }
        if (product.getTool() != null) to.setToolId(product.getTool().getId());

        if (product.getMeasuringFeaturePrototypes() != null) {
            to.setMeasuringFeaturePrototypes(
                    product.getMeasuringFeaturePrototypes()
                            .stream()
                            .map(this::mapPrototypeToTO)
                            .toList()
            );
        }
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
        // On update, omitting reference in JSON leaves it unchanged; explicit null/empty clears or sets.
        if (to.getId() == null) {
            product.setReference(to.getReference());
        } else if (to.getReference() != null) {
            product.setReference(to.getReference());
        }
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

        if (to.getMeasuringFeaturePrototypes() != null) {
            List<MeasuringFeaturePrototype> prototypes = to.getMeasuringFeaturePrototypes()
                    .stream()
                    .map(this::mapPrototypeTOToEntity)
                    .peek(p -> p.setProduct(product))
                    .toList();
            product.setMeasuringFeaturePrototypes(prototypes);
        }
        return product;
    }

    private MeasuringFeaturePrototypeTO mapPrototypeToTO(MeasuringFeaturePrototype p) {
        if (p == null) return null;
        return new MeasuringFeaturePrototypeTO(
                p.getId(),
                p.getCatalogueId(),
                p.getDescription(),
                p.isAbsoluteMeasure(),
                p.getRefValue(),
                p.getMinTolerance(),
                p.getMaxTolerance(),
                p.getClassType(),
                p.getFrequency(),
                p.getCheckType(),
                p.getToolType(),
                p.getMeasuringTool()
        );
    }

    private MeasuringFeaturePrototype mapPrototypeTOToEntity(MeasuringFeaturePrototypeTO to) {
        if (to == null) return null;
        MeasuringFeaturePrototype entity = new MeasuringFeaturePrototype();
        entity.setId(to.getId());
        entity.setCatalogueId(to.getCatalogueId());
        entity.setDescription(to.getDescription());
        entity.setAbsoluteMeasure(to.getAbsoluteMeasure() != null ? to.getAbsoluteMeasure() : false);
        entity.setRefValue(to.getRefValue());
        entity.setMinTolerance(to.getMinTolerance());
        entity.setMaxTolerance(to.getMaxTolerance());
        entity.setClassType(to.getClassType());
        entity.setFrequency(to.getFrequency());
        entity.setCheckType(to.getCheckType());
        entity.setToolType(to.getToolType());
        entity.setMeasuringTool(to.getMeasuringTool());
        return entity;
    }
}

