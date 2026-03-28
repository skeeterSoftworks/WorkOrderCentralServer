package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.*;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.WorkOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.WorkSessionRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EWorkOrderState;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class WorkSessionService {

    private final WorkSessionRepository workSessionRepository;
    private final WorkOrderRepository workOrderRepository;
    private final MachineBookingService machineBookingService;

    @Autowired
    public WorkSessionService(
            WorkSessionRepository workSessionRepository,
            WorkOrderRepository workOrderRepository,
            MachineBookingService machineBookingService
    ) {
        this.workSessionRepository = workSessionRepository;
        this.workOrderRepository = workOrderRepository;
        this.machineBookingService = machineBookingService;
    }

    @Transactional(readOnly = true)
    public WorkSession getById(long id) throws Exception {
        WorkSession session = workSessionRepository.findById(id).orElseThrow(() -> new Exception("WORK_SESSION_NOT_FOUND"));
        preloadMeasuringFeaturePrototypes(session);
        return session;
    }

    @Transactional
    public WorkSession openSession(WorkSessionOpenRequestTO req) throws Exception {
        if (req.getWorkOrderId() == null || req.getWorkOrderId() <= 0) {
            throw new Exception("INVALID_WORK_ORDER_ID");
        }
        WorkOrder workOrder = workOrderRepository.findById(req.getWorkOrderId())
                .orElseThrow(() -> new Exception("WORK_ORDER_NOT_FOUND"));

        throwIfWorkOrderNotOpenForProduction(workOrder);

        ProductOrder po = workOrder.getProductOrder();
        Product product = po != null ? po.getProduct() : null;
        if (product == null) {
            throw new Exception("PRODUCT_NOT_FOUND_FOR_WORK_ORDER");
        }
        if (product.getQualityInfoSteps() != null) {
            product.getQualityInfoSteps().size();
        }
        if (product.getQualityInfoSteps() == null || product.getQualityInfoSteps().isEmpty()) {
            throw new Exception("QUALITY_INFO_STEPS_REQUIRED");
        }

        WorkSession session = new WorkSession();
        session.setWorkOrder(workOrder);
        session.setSessionStart(LocalDateTime.now());
        session.setProductCount(0L);

        Operator op = new Operator();
        op.setOperatorQrCode(req.getOperatorQrCode());
        op.setName(req.getOperatorName());
        op.setSurname(req.getOperatorSurname());
        session.setOperator(op);

        StationInfo station = new StationInfo();
        station.setStationID(req.getStationId() != null ? req.getStationId() : "");
        session.setStationInfo(station);

        preloadMeasuringFeaturePrototypes(workOrder);
        return workSessionRepository.save(session);
    }

    private void throwIfWorkOrderNotOpenForProduction(WorkOrder workOrder) throws Exception {
        if (workOrder.getState() == EWorkOrderState.COMPLETE) {
            throw new Exception("WORK_ORDER_COMPLETE");
        }
        ProductOrder po = workOrder.getProductOrder();
        if (po != null && po.getQuantity() > 0 && workOrder.getProducedGoodQuantity() >= po.getQuantity()) {
            throw new Exception("WORK_ORDER_COMPLETE");
        }
    }

    @Transactional
    public WorkSession endSession(long sessionId) throws Exception {
        WorkSession session = getById(sessionId);
        if (session.getSessionEnd() != null) {
            throw new Exception("WORK_SESSION_ALREADY_ENDED");
        }
        session.setSessionEnd(LocalDateTime.now());
        preloadMeasuringFeaturePrototypes(session);
        return workSessionRepository.save(session);
    }

    @Transactional
    public WorkSessionIncrementResult incrementProductCount(long sessionId, ProductCountDeltaRequestTO req) throws Exception {
        if (req.getDelta() <= 0) {
            throw new Exception("INVALID_DELTA");
        }
        WorkSession session = getById(sessionId);
        if (session.getSessionEnd() != null) {
            throw new Exception("WORK_SESSION_ALREADY_ENDED");
        }
        session.setProductCount(session.getProductCount() + req.getDelta());
        if (req.getProductReferenceID() != null && !req.getProductReferenceID().isBlank()) {
            session.setProductReferenceID(req.getProductReferenceID().trim());
        }
        workSessionRepository.save(session);
        boolean completedByTarget = syncWorkOrderProducedQuantityAndCompleteIfReached(session.getId());
        WorkSession latest = workSessionRepository.findById(sessionId).orElseThrow();
        preloadMeasuringFeaturePrototypes(latest);
        return new WorkSessionIncrementResult(latest, completedByTarget);
    }

    @Transactional
    public WorkSession addControlProduct(long sessionId, ControlProductCreateRequestTO req) throws Exception {
        Objects.requireNonNull(req, "request");
        if (req.getMeasuringFeatures() == null || req.getMeasuringFeatures().isEmpty()) {
            throw new Exception("MEASURING_FEATURES_REQUIRED");
        }

        WorkSession session = getById(sessionId);
        if (session.getSessionEnd() != null) {
            throw new Exception("WORK_SESSION_ALREADY_ENDED");
        }

        ProductOrder po = session.getWorkOrder() != null ? session.getWorkOrder().getProductOrder() : null;
        Product product = po != null ? po.getProduct() : null;
        if (product == null || product.getMeasuringFeaturePrototypes() == null) {
            throw new Exception("MEASURING_FEATURE_PROTOTYPES_REQUIRED");
        }
        var prototypesByCatalogueId = product.getMeasuringFeaturePrototypes()
                .stream()
                .filter(p -> p.getCatalogueId() != null && !p.getCatalogueId().isBlank())
                .collect(java.util.stream.Collectors.toMap(
                        MeasuringFeaturePrototype::getCatalogueId,
                        p -> p,
                        (a, b) -> a
                ));

        ControlProduct cp = new ControlProduct();
        cp.setWorkSession(session);
        cp.setCreatedAt(LocalDateTime.now());

        for (MeasuringFeatureInputTO in : req.getMeasuringFeatures()) {
            MeasuringFeature mf = new MeasuringFeature();
            mf.setControlProduct(cp);

            if (in.getCatalogueId() == null || in.getCatalogueId().isBlank()) {
                throw new Exception("CATALOGUE_ID_REQUIRED");
            }
            MeasuringFeaturePrototype proto = prototypesByCatalogueId.get(in.getCatalogueId().trim());
            if (proto == null) {
                throw new Exception("MEASURING_FEATURE_PROTOTYPE_NOT_FOUND");
            }

            mf.setCatalogueId(proto.getCatalogueId());
            mf.setDescription(proto.getDescription());
            mf.setRefValue(proto.getRefValue());
            mf.setMinTolerance(proto.getMinTolerance());
            mf.setMaxTolerance(proto.getMaxTolerance());
            mf.setClassType(proto.getClassType());
            mf.setFrequency(proto.getFrequency());
            mf.setCheckType(proto.getCheckType());
            mf.setToolType(proto.getToolType());
            mf.setMeasuringTool(proto.getMeasuringTool());

            mf.setAssessedValue(in.getAssessedValue());
            mf.setAssessedValueGood(in.isAssessedValueGood());

            cp.getMeasuringFeatures().add(mf);
        }

        session.getControlProducts().add(cp);
        preloadMeasuringFeaturePrototypes(session);
        return workSessionRepository.save(session);
    }

    private void preloadMeasuringFeaturePrototypes(WorkSession session) {
        if (session == null || session.getWorkOrder() == null) return;
        preloadMeasuringFeaturePrototypes(session.getWorkOrder());
    }

    private void preloadMeasuringFeaturePrototypes(WorkOrder workOrder) {
        if (workOrder == null || workOrder.getProductOrder() == null) return;
        Product product = workOrder.getProductOrder().getProduct();
        if (product == null || product.getMeasuringFeaturePrototypes() == null) return;
        // Force initialization of lazy collection.
        product.getMeasuringFeaturePrototypes().size();
    }

    @Transactional
    public WorkSession addFaultyProduct(long sessionId, FaultyProductCreateRequestTO req) throws Exception {
        WorkSession session = getById(sessionId);
        if (session.getSessionEnd() != null) {
            throw new Exception("WORK_SESSION_ALREADY_ENDED");
        }

        FaultyProduct fp = new FaultyProduct();
        fp.setWorkSession(session);
        fp.setRejectReason(req != null ? req.getRejectReason() : null);
        fp.setRejectCause(req != null ? req.getRejectCause() : null);
        fp.setRejectComment(req != null ? req.getRejectComment() : null);
        fp.setCreatedAt(LocalDateTime.now());

        session.getFaultyProducts().add(fp);
        return workSessionRepository.save(session);
    }

    /**
     * Recomputes {@link WorkOrder#getProducedGoodQuantity()} from all sessions. If it reaches or exceeds the
     * product-order line quantity, marks the work order {@link EWorkOrderState#COMPLETE} and ends the given session.
     *
     * @param sessionId id of the session that was just updated (reloaded after persistence)
     * @return {@code true} if the work order was completed and the session was auto-closed
     */
    private boolean syncWorkOrderProducedQuantityAndCompleteIfReached(long sessionId) {
        WorkSession session = workSessionRepository.findById(sessionId).orElse(null);
        if (session == null || session.getWorkOrder() == null) {
            return false;
        }
        Long workOrderId = session.getWorkOrder().getId();
        if (workOrderId == null) {
            return false;
        }
        WorkOrder workOrder = workOrderRepository.findById(workOrderId).orElse(null);
        if (workOrder == null) {
            return false;
        }
        long produced = workSessionRepository.sumProductCountByWorkOrderId(workOrderId);
        workOrder.setProducedGoodQuantity(produced);
        ProductOrder po = workOrder.getProductOrder();
        if (po == null) {
            workOrderRepository.save(workOrder);
            return false;
        }
        int required = po.getQuantity();
        if (required <= 0) {
            workOrderRepository.save(workOrder);
            return false;
        }
        if ((workOrder.getState() != null && workOrder.getState() == EWorkOrderState.COMPLETE) || produced < required) {
            workOrderRepository.save(workOrder);
            return false;
        }
        workOrder.setState(EWorkOrderState.COMPLETE);
        workOrderRepository.save(workOrder);
        machineBookingService.completeNonCancelledBookingsForWorkOrder(workOrder);

        WorkSession toClose = workSessionRepository.findById(sessionId).orElse(session);
        if (toClose.getSessionEnd() == null) {
            toClose.setSessionEnd(LocalDateTime.now());
            workSessionRepository.save(toClose);
        }
        return true;
    }
}
