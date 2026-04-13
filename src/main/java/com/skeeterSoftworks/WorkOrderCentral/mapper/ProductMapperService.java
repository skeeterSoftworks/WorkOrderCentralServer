package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Customer;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Machine;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MeasuringFeaturePrototype;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.QualityInfoStep;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.SetupDataPrototype;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Technology;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Tool;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.CustomerRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MachineRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.TechnologyRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MeasuringFeaturePrototypeTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.QualityInfoStepTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.SetupDataPrototypeTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.TechnologyTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ToolTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductMapperService {

    private final MachineRepository machineRepository;
    private final CustomerRepository customerRepository;
    private final TechnologyRepository technologyRepository;

    @Autowired
    public ProductMapperService(
            MachineRepository machineRepository,
            CustomerRepository customerRepository,
            TechnologyRepository technologyRepository) {
        this.machineRepository = machineRepository;
        this.customerRepository = customerRepository;
        this.technologyRepository = technologyRepository;
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
        if (product.getCustomers() != null && !product.getCustomers().isEmpty()) {
            to.setCustomerIds(product.getCustomers().stream().map(Customer::getId).collect(Collectors.toList()));
        } else {
            to.setCustomerIds(Collections.emptyList());
        }

        to.setSetupDataPrototype(mapSetupPrototypeToTO(product.getSetupDataPrototype()));
        to.setTechnologyData(mapTechnologyToTO(product.getTechnologyData()));

        if (product.getMeasuringFeaturePrototypes() != null) {
            to.setMeasuringFeaturePrototypes(
                    product.getMeasuringFeaturePrototypes()
                            .stream()
                            .map(this::mapPrototypeToTO)
                            .toList()
            );
        }

        if (product.getQualityInfoSteps() != null) {
            to.setQualityInfoSteps(
                    product.getQualityInfoSteps().stream()
                            .map(this::mapQualityStepToTO)
                            .sorted(Comparator.comparing(QualityInfoStepTO::getStepNumber, Comparator.nullsLast(Integer::compareTo)))
                            .toList()
            );
        }
        if (product.getTechnicalDrawing() != null && product.getTechnicalDrawing().length > 0) {
            to.setTechnicalDrawingBase64(Base64.getEncoder().encodeToString(product.getTechnicalDrawing()));
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
        if (to.getCustomerIds() != null && !to.getCustomerIds().isEmpty()) {
            List<Customer> customerList = new ArrayList<>();
            for (Long id : to.getCustomerIds()) {
                customerRepository.findById(id).ifPresent(customerList::add);
            }
            product.setCustomers(customerList);
        } else {
            product.setCustomers(new ArrayList<>());
        }

        product.setSetupDataPrototype(mapSetupPrototypeTOToEntity(to.getSetupDataPrototype()));
        product.setTechnologyData(mapTechnologyTOToEntity(to.getTechnologyData()));

        if (to.getMeasuringFeaturePrototypes() != null) {
            List<MeasuringFeaturePrototype> prototypes = to.getMeasuringFeaturePrototypes()
                    .stream()
                    .map(this::mapPrototypeTOToEntity)
                    .peek(p -> p.setProduct(product))
                    .toList();
            product.setMeasuringFeaturePrototypes(prototypes);
        }
        if (to.getQualityInfoSteps() != null) {
            List<QualityInfoStep> steps = to.getQualityInfoSteps().stream()
                    .map(this::mapQualityStepTOToEntity)
                    .peek(s -> s.setProduct(product))
                    .toList();
            product.setQualityInfoSteps(steps);
        }
        if (to.getId() == null) {
            product.setTechnicalDrawing(decodeBase64Image(to.getTechnicalDrawingBase64()));
        } else if (to.getTechnicalDrawingBase64() != null) {
            product.setTechnicalDrawing(decodeBase64Image(to.getTechnicalDrawingBase64()));
        }
        return product;
    }

    public TechnologyTO mapTechnologyToTO(Technology entity) {
        if (entity == null) {
            return null;
        }
        TechnologyTO t = new TechnologyTO();
        t.setId(entity.getId());
        t.setCycleTime(entity.getCycleTime());
        t.setNorm100(entity.getNorm100());
        t.setPiecesPerMaterial(entity.getPiecesPerMaterial());
        if (entity.getTools() != null && !entity.getTools().isEmpty()) {
            t.setTools(entity.getTools().stream()
                    .map(this::mapToolEntityToTO)
                    .sorted(Comparator.comparing(ToolTO::getOrderNumber, Comparator.nullsLast(Integer::compareTo))
                            .thenComparing(ToolTO::getId, Comparator.nullsLast(Long::compareTo)))
                    .toList());
        } else {
            t.setTools(new ArrayList<>());
        }
        return t;
    }

    private ToolTO mapToolEntityToTO(Tool tool) {
        if (tool == null) {
            return null;
        }
        ToolTO t = new ToolTO();
        t.setId(tool.getId());
        t.setToolName(tool.getToolName());
        t.setToolDescription(tool.getToolDescription());
        t.setOrderNumber(tool.getOrderNumber());
        t.setWorkingTime(tool.getWorkingTime());
        if (tool.getTechnology() != null) {
            t.setTechnologyId(tool.getTechnology().getId());
        }
        return t;
    }

    private Technology mapTechnologyTOToEntity(TechnologyTO to) {
        if (to == null || isTechnologyTOEffectivelyEmpty(to)) {
            return null;
        }
        if (to.getId() != null) {
            return technologyRepository.findById(to.getId())
                    .map(existing -> {
                        applyTechnologyFields(existing, to);
                        ensureTechnologyToolsList(existing);
                        syncTechnologyTools(existing, to.getTools());
                        return existing;
                    })
                    .orElseGet(() -> newTechnologyFromTo(to));
        }
        return newTechnologyFromTo(to);
    }

    private void ensureTechnologyToolsList(Technology tech) {
        if (tech.getTools() == null) {
            tech.setTools(new ArrayList<>());
        }
    }

    private Technology newTechnologyFromTo(TechnologyTO to) {
        Technology t = new Technology();
        applyTechnologyFields(t, to);
        t.setTools(new ArrayList<>());
        syncTechnologyTools(t, to.getTools());
        return t;
    }

    private void syncTechnologyTools(Technology tech, List<ToolTO> incoming) {
        ensureTechnologyToolsList(tech);
        List<ToolTO> list = incoming != null ? incoming : List.of();
        tech.getTools().removeIf(tool ->
                tool.getId() != null && list.stream().noneMatch(tt -> tool.getId().equals(tt.getId())));
        for (ToolTO tt : list) {
            Tool tool;
            if (tt.getId() != null) {
                tool = tech.getTools().stream()
                        .filter(t -> tt.getId().equals(t.getId()))
                        .findFirst()
                        .orElseGet(() -> {
                            Tool n = new Tool();
                            tech.getTools().add(n);
                            return n;
                        });
            } else {
                tool = new Tool();
                tech.getTools().add(tool);
            }
            tool.setToolName(tt.getToolName());
            tool.setToolDescription(tt.getToolDescription());
            tool.setOrderNumber(tt.getOrderNumber());
            tool.setWorkingTime(tt.getWorkingTime());
            tool.setTechnology(tech);
        }
    }

    private static void applyTechnologyFields(Technology target, TechnologyTO to) {
        target.setCycleTime(to.getCycleTime());
        target.setNorm100(to.getNorm100());
        target.setPiecesPerMaterial(to.getPiecesPerMaterial());
    }

    private static boolean isTechnologyTOEffectivelyEmpty(TechnologyTO to) {
        if (to.getTools() != null && !to.getTools().isEmpty()) {
            return false;
        }
        boolean noStrings = (to.getCycleTime() == null || to.getCycleTime().isBlank());
        boolean noNumbers = to.getPiecesPerMaterial() == null
                && to.getNorm100() == null;
        return noStrings && noNumbers;
    }

    public SetupDataPrototypeTO mapSetupPrototypeToTO(SetupDataPrototype s) {
        if (s == null) return null;
        return new SetupDataPrototypeTO(
                s.getOperationID(),
                s.getToolID(),
                s.getDiameterRefValue(),
                s.getDiameterMaxPosTolerance(),
                s.getDiameterMaxNegTolerance(),
                s.getHeightRefValue(),
                s.getHeightMaxPosTolerance(),
                s.getHeightMaxNegTolerance(),
                s.isAttributiveHeightMeasurement(),
                s.isAttributiveDiameterMeasurement()
        );
    }

    private SetupDataPrototype mapSetupPrototypeTOToEntity(SetupDataPrototypeTO to) {
        if (to == null || isSetupDataPrototypeEffectivelyEmpty(to)) {
            return null;
        }
        SetupDataPrototype s = new SetupDataPrototype();
        s.setOperationID(to.getOperationID());
        s.setToolID(to.getToolID());
        s.setDiameterRefValue(to.getDiameterRefValue());
        s.setDiameterMaxPosTolerance(to.getDiameterMaxPosTolerance());
        s.setDiameterMaxNegTolerance(to.getDiameterMaxNegTolerance());
        s.setHeightRefValue(to.getHeightRefValue());
        s.setHeightMaxPosTolerance(to.getHeightMaxPosTolerance());
        s.setHeightMaxNegTolerance(to.getHeightMaxNegTolerance());
        s.setAttributiveHeightMeasurement(Boolean.TRUE.equals(to.getAttributiveHeightMeasurement()));
        s.setAttributiveDiameterMeasurement(Boolean.TRUE.equals(to.getAttributiveDiameterMeasurement()));
        return s;
    }

    private boolean isSetupDataPrototypeEffectivelyEmpty(SetupDataPrototypeTO to) {
        boolean noIds = (to.getOperationID() == null || to.getOperationID().isBlank())
                && (to.getToolID() == null || to.getToolID().isBlank());
        boolean noMeasures = to.getDiameterRefValue() == null
                && to.getDiameterMaxPosTolerance() == null
                && to.getDiameterMaxNegTolerance() == null
                && to.getHeightRefValue() == null
                && to.getHeightMaxPosTolerance() == null
                && to.getHeightMaxNegTolerance() == null;
        boolean noFlags = !Boolean.TRUE.equals(to.getAttributiveHeightMeasurement())
                && !Boolean.TRUE.equals(to.getAttributiveDiameterMeasurement());
        return noIds && noMeasures && noFlags;
    }

    private MeasuringFeaturePrototypeTO mapPrototypeToTO(MeasuringFeaturePrototype p) {
        if (p == null) return null;
        return new MeasuringFeaturePrototypeTO(
                p.getId(),
                p.getCatalogueId(),
                p.getDescription(),
                p.getRefValue(),
                p.getMinTolerance(),
                p.getMaxTolerance(),
                p.getClassType(),
                p.getFrequency(),
                p.getCheckType(),
                p.getMeasuringTool()
        );
    }

    private MeasuringFeaturePrototype mapPrototypeTOToEntity(MeasuringFeaturePrototypeTO to) {
        if (to == null) return null;
        MeasuringFeaturePrototype entity = new MeasuringFeaturePrototype();
        entity.setId(to.getId());
        entity.setCatalogueId(to.getCatalogueId());
        entity.setDescription(to.getDescription());
        entity.setRefValue(to.getRefValue());
        entity.setMinTolerance(to.getMinTolerance());
        entity.setMaxTolerance(to.getMaxTolerance());
        entity.setClassType(to.getClassType());
        entity.setFrequency(to.getFrequency());
        entity.setCheckType(to.getCheckType());
        entity.setMeasuringTool(to.getMeasuringTool());
        return entity;
    }

    /** Ordered list for API responses (e.g. work-order quality steps). */
    public List<QualityInfoStepTO> toQualityInfoStepTOList(List<QualityInfoStep> steps) {
        if (steps == null || steps.isEmpty()) {
            return Collections.emptyList();
        }
        return steps.stream()
                .map(this::mapQualityStepToTO)
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(QualityInfoStepTO::getStepNumber, Comparator.nullsLast(Integer::compareTo)))
                .toList();
    }

    private QualityInfoStepTO mapQualityStepToTO(QualityInfoStep s) {
        if (s == null) return null;
        String b64 = null;
        if (s.getImageData() != null && s.getImageData().length > 0) {
            b64 = Base64.getEncoder().encodeToString(s.getImageData());
        }
        return new QualityInfoStepTO(s.getId(), s.getStepNumber(), s.getStepDescription(), b64);
    }

    public QualityInfoStep mapQualityStepTOToEntity(QualityInfoStepTO to) {
        if (to == null) return null;
        QualityInfoStep entity = new QualityInfoStep();
        entity.setId(to.getId());
        entity.setStepNumber(to.getStepNumber());
        entity.setStepDescription(to.getStepDescription());
        entity.setImageData(decodeBase64Image(to.getImageDataBase64()));
        return entity;
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

