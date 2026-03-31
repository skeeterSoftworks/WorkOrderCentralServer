package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.SetupProduct;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkSession;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.SetupProductTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.WorkSessionTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Comparator;
import java.util.List;

@Service
public class WorkSessionMapperService {

    private final ProductMapperService productMapperService;

    @Autowired
    public WorkSessionMapperService(ProductMapperService productMapperService) {
        this.productMapperService = productMapperService;
    }

    public WorkSessionTO mapToTO(WorkSession session) {
        if (session == null) {
            return null;
        }
        WorkSessionTO to = new WorkSessionTO();
        to.setId(session.getId());
        if (session.getWorkOrder() != null) {
            to.setWorkOrderId(session.getWorkOrder().getId());
        }
        to.setSessionStart(session.getSessionStart());
        to.setSessionEnd(session.getSessionEnd());
        to.setProductCount(session.getProductCount());
        long controlCount = 0L;
        if (session.getControlProducts() != null) {
            controlCount = session.getControlProducts().size();
        }
        long faultyCount = 0L;
        if (session.getFaultyProducts() != null) {
            faultyCount = session.getFaultyProducts().size();
        }
        to.setControlProductCount(controlCount);
        to.setFaultyProductCount(faultyCount);
        long setupCount = session.getSetupProductCount() == null ? 0L : session.getSetupProductCount();
        to.setSetupProductCount(setupCount);
        if (session.getSetupProducts() != null && !session.getSetupProducts().isEmpty()) {
            to.setSetupProducts(session.getSetupProducts().stream()
                    .sorted(Comparator.comparing(SetupProduct::getRecordedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(sp -> sp.getId() != null ? sp.getId() : 0L))
                    .map(this::mapSetupProduct)
                    .toList());
        } else {
            to.setSetupProducts(List.of());
        }
        to.setProductReferenceID(session.getProductReferenceID());
        if (session.getOperator() != null) {
            to.setOperatorQrCode(session.getOperator().getOperatorQrCode());
            to.setOperatorName(session.getOperator().getName());
            to.setOperatorSurname(session.getOperator().getSurname());
        }
        if (session.getStationInfo() != null) {
            to.setStationId(session.getStationInfo().getStationID());
        }
        if (session.getWorkOrder() != null
                && session.getWorkOrder().getProductOrder() != null
                && session.getWorkOrder().getProductOrder().getProduct() != null) {
            var product = session.getWorkOrder().getProductOrder().getProduct();
            // This list is initialized inside WorkSessionService before mapping to avoid LazyInitializationException.
            to.setMeasuringFeaturePrototypes(
                    product.getMeasuringFeaturePrototypes()
                            .stream()
                            .map(p -> new com.skeeterSoftworks.WorkOrderCentral.to.objects.MeasuringFeaturePrototypeTO(
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
                            ))
                            .toList()
            );
            if (product.getTechnicalDrawing() != null && product.getTechnicalDrawing().length > 0) {
                to.setTechnicalDrawingBase64(Base64.getEncoder().encodeToString(product.getTechnicalDrawing()));
            }
        }
        to.setWorkOrderCompletedByTarget(false);
        return to;
    }

    private SetupProductTO mapSetupProduct(SetupProduct sp) {
        return new SetupProductTO(
                sp.getId(),
                sp.getRecordedAt(),
                productMapperService.mapSetupPrototypeToTO(sp.getPrototypeSnapshot()),
                sp.getMeasuredHeight(),
                sp.getMeasuredHeightOk(),
                sp.getMeasuredDiameter(),
                sp.getMeasuredDiameterOk()
        );
    }

    public WorkSessionTO mapToTO(WorkSession session, boolean workOrderCompletedByTarget) {
        WorkSessionTO to = mapToTO(session);
        to.setWorkOrderCompletedByTarget(workOrderCompletedByTarget);
        return to;
    }
}
