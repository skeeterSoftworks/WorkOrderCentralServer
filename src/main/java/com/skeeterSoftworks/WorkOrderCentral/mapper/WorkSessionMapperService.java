package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkSession;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.WorkSessionTO;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class WorkSessionMapperService {

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
                                    p.isAbsoluteMeasure(),
                                    p.getRefValue(),
                                    p.getMinTolerance(),
                                    p.getMaxTolerance(),
                                    p.getClassType(),
                                    p.getFrequency(),
                                    p.getCheckType(),
                                    p.getToolType(),
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

    public WorkSessionTO mapToTO(WorkSession session, boolean workOrderCompletedByTarget) {
        WorkSessionTO to = mapToTO(session);
        to.setWorkOrderCompletedByTarget(workOrderCompletedByTarget);
        return to;
    }
}
