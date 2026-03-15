package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.WorkOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;

    @Autowired
    public WorkOrderService(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }

    public List<WorkOrder> getAllWorkOrders() {
        return workOrderRepository.findAll();
    }

    public Optional<WorkOrder> getWorkOrderById(Long id) {
        return workOrderRepository.findById(id);
    }

    public WorkOrder addWorkOrder(WorkOrder workOrder) {
        workOrder.setId(null);
        return workOrderRepository.save(workOrder);
    }

    public WorkOrder updateWorkOrder(WorkOrder workOrder) throws Exception {
        if (workOrder.getId() == null || workOrder.getId() <= 0) {
            throw new Exception("INVALID_ID");
        }
        WorkOrder existing = workOrderRepository.findById(workOrder.getId())
                .orElseThrow(() -> new Exception("WORK_ORDER_NOT_FOUND"));
        existing.setPurchaseOrder(workOrder.getPurchaseOrder());
        existing.setDueDate(workOrder.getDueDate());
        existing.setStartDate(workOrder.getStartDate());
        existing.setEndDate(workOrder.getEndDate());
        existing.setComment(workOrder.getComment());
        return workOrderRepository.save(existing);
    }

    public void deleteWorkOrder(Long id) throws Exception {
        if (!workOrderRepository.existsById(id)) {
            throw new Exception("WORK_ORDER_NOT_FOUND");
        }
        workOrderRepository.deleteById(id);
    }
}
