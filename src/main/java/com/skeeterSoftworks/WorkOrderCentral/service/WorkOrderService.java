package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MachineBookingRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.WorkOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.mapper.ProductMapperService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.QualityInfoStepTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final PurchaseOrderService purchaseOrderService;
    private final ProductOrderRepository productOrderRepository;
    private final MachineBookingRepository machineBookingRepository;
    private final ProductMapperService productMapperService;

    @Autowired
    public WorkOrderService(
            WorkOrderRepository workOrderRepository,
            PurchaseOrderService purchaseOrderService,
            ProductOrderRepository productOrderRepository,
            MachineBookingRepository machineBookingRepository,
            ProductMapperService productMapperService
    ) {
        this.workOrderRepository = workOrderRepository;
        this.purchaseOrderService = purchaseOrderService;
        this.productOrderRepository = productOrderRepository;
        this.machineBookingRepository = machineBookingRepository;
        this.productMapperService = productMapperService;
    }

    public List<WorkOrder> getAllWorkOrders() {
        return workOrderRepository.findAll();
    }

    /**
     * Work orders that have at least one non-cancelled {@link com.skeeterSoftworks.WorkOrderCentral.domain.objects.MachineBooking}
     * on the given machine.
     */
    public List<WorkOrder> getWorkOrdersForMachine(Long machineId) {
        if (machineId == null || machineId <= 0) {
            return Collections.emptyList();
        }
        List<Long> ids = machineBookingRepository.findWorkOrderIdsScheduledOnMachine(machineId);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return workOrderRepository.findAllByIdIn(ids);
    }

    public Optional<WorkOrder> getWorkOrderById(Long id) {
        return workOrderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<QualityInfoStepTO> getQualityInfoStepsForWorkOrder(Long workOrderId) throws Exception {
        if (workOrderId == null || workOrderId <= 0) {
            throw new Exception("INVALID_WORK_ORDER_ID");
        }
        WorkOrder wo = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new Exception("WORK_ORDER_NOT_FOUND"));
        ProductOrder po = wo.getProductOrder();
        if (po == null || po.getProduct() == null) {
            return Collections.emptyList();
        }
        Product product = po.getProduct();
        if (product.getQualityInfoSteps() != null) {
            product.getQualityInfoSteps().size();
        }
        return productMapperService.toQualityInfoStepTOList(product.getQualityInfoSteps());
    }

    public WorkOrder addWorkOrder(WorkOrder workOrder) throws Exception {
        if (workOrder.getProductOrder() == null || workOrder.getProductOrder().getId() <= 0) {
            throw new Exception("INVALID_PRODUCT_ORDER");
        }
        long lineId = workOrder.getProductOrder().getId();
        if (!productOrderRepository.existsById(lineId)) {
            throw new Exception("PRODUCT_ORDER_NOT_FOUND");
        }
        if (workOrderRepository.existsByProductOrder_Id(lineId)) {
            throw new Exception("WORK_ORDER_ALREADY_EXISTS_FOR_PRODUCT_ORDER");
        }
        workOrder.setId(null);
        WorkOrder saved = workOrderRepository.save(workOrder);
        productOrderRepository.findPurchaseOrderIdByProductOrderLineId(lineId)
                .ifPresent(purchaseOrderService::markConfirmed);
        return saved;
    }

    public WorkOrder updateWorkOrder(WorkOrder workOrder) throws Exception {
        if (workOrder.getId() == null || workOrder.getId() <= 0) {
            throw new Exception("INVALID_ID");
        }
        WorkOrder existing = workOrderRepository.findById(workOrder.getId())
                .orElseThrow(() -> new Exception("WORK_ORDER_NOT_FOUND"));
        if (workOrder.getProductOrder() == null || workOrder.getProductOrder().getId() <= 0) {
            throw new Exception("INVALID_PRODUCT_ORDER");
        }
        long newLineId = workOrder.getProductOrder().getId();
        if (!productOrderRepository.existsById(newLineId)) {
            throw new Exception("PRODUCT_ORDER_NOT_FOUND");
        }
        Long existingLineId = existing.getProductOrder() != null ? existing.getProductOrder().getId() : null;
        if (!Objects.equals(newLineId, existingLineId) && workOrderRepository.existsByProductOrder_Id(newLineId)) {
            throw new Exception("WORK_ORDER_ALREADY_EXISTS_FOR_PRODUCT_ORDER");
        }
        existing.setProductOrder(workOrder.getProductOrder());
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
