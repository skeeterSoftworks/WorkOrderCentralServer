package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkSession;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.WorkSessionTO;
import org.springframework.stereotype.Service;

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
            // This list is initialized inside WorkSessionService before mapping to avoid LazyInitializationException.
            to.setMeasuringFeaturePrototypes(
                    session.getWorkOrder()
                            .getProductOrder()
                            .getProduct()
                            .getMeasuringFeaturePrototypes()
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
