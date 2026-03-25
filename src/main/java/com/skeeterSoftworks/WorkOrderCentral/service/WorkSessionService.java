package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.*;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.WorkOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.WorkSessionRepository;
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

    @Autowired
    public WorkSessionService(WorkSessionRepository workSessionRepository, WorkOrderRepository workOrderRepository) {
        this.workSessionRepository = workSessionRepository;
        this.workOrderRepository = workOrderRepository;
    }

    @Transactional(readOnly = true)
    public WorkSession getById(long id) throws Exception {
        return workSessionRepository.findById(id).orElseThrow(() -> new Exception("WORK_SESSION_NOT_FOUND"));
    }

    @Transactional
    public WorkSession openSession(WorkSessionOpenRequestTO req) throws Exception {
        if (req.getWorkOrderId() == null || req.getWorkOrderId() <= 0) {
            throw new Exception("INVALID_WORK_ORDER_ID");
        }
        WorkOrder workOrder = workOrderRepository.findById(req.getWorkOrderId())
                .orElseThrow(() -> new Exception("WORK_ORDER_NOT_FOUND"));

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

        return workSessionRepository.save(session);
    }

    @Transactional
    public WorkSession endSession(long sessionId) throws Exception {
        WorkSession session = getById(sessionId);
        if (session.getSessionEnd() != null) {
            throw new Exception("WORK_SESSION_ALREADY_ENDED");
        }
        session.setSessionEnd(LocalDateTime.now());
        return workSessionRepository.save(session);
    }

    @Transactional
    public WorkSession incrementProductCount(long sessionId, ProductCountDeltaRequestTO req) throws Exception {
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
        WorkSession saved = workSessionRepository.save(session);
        syncWorkOrderProducedGoodQuantity(saved);
        return saved;
    }

    @Transactional
    public WorkSession addControlProduct(long sessionId, ControlProductCreateRequestTO req) throws Exception {
        Objects.requireNonNull(req, "request");
        if (req.getMeasuringFeatures() == null || req.getMeasuringFeatures().isEmpty()) {
            throw new Exception("MEASURING_FEATURES_REQUIRED");
        }
        for (MeasuringFeatureInputTO in : req.getMeasuringFeatures()) {
            if (in.getFeatureName() == null || in.getFeatureName().isBlank()) {
                throw new Exception("FEATURE_NAME_REQUIRED");
            }
        }

        WorkSession session = getById(sessionId);
        if (session.getSessionEnd() != null) {
            throw new Exception("WORK_SESSION_ALREADY_ENDED");
        }

        ControlProduct cp = new ControlProduct();
        cp.setWorkSession(session);
        cp.setCreatedAt(LocalDateTime.now());

        for (MeasuringFeatureInputTO in : req.getMeasuringFeatures()) {
            MeasuringFeature mf = new MeasuringFeature();
            mf.setControlProduct(cp);
            mf.setFeatureName(in.getFeatureName().trim());
            mf.setWidth(in.getWidth());
            mf.setHeight(in.getHeight());
            mf.setDepth(in.getDepth());
            mf.setDiameter(in.getDiameter());
            cp.getMeasuringFeatures().add(mf);
        }

        session.getControlProducts().add(cp);
        return workSessionRepository.save(session);
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

    private void syncWorkOrderProducedGoodQuantity(WorkSession session) {
        if (session == null || session.getWorkOrder() == null) {
            return;
        }
        WorkOrder workOrder = session.getWorkOrder();
        long produced = workOrder.getWorkSessions().stream()
                .mapToLong(WorkSession::getProductCount)
                .sum();
        workOrder.setProducedGoodQuantity(produced);
        workOrderRepository.save(workOrder);
    }
}
