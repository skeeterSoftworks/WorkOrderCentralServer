package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Customer;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrderLine;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.PurchaseOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.MaterialOrderLineRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderHistoryPageTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderHistoryRowTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductOrderHistoryPageTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductOrderHistoryRowTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrdersHistoryService {

    private static final int DEFAULT_SIZE = 25;
    private static final int MAX_SIZE = 100;

    private final ProductOrderRepository productOrderRepository;
    private final MaterialOrderLineRepository materialOrderLineRepository;

    public OrdersHistoryService(
            ProductOrderRepository productOrderRepository,
            MaterialOrderLineRepository materialOrderLineRepository) {
        this.productOrderRepository = productOrderRepository;
        this.materialOrderLineRepository = materialOrderLineRepository;
    }

    @Transactional(readOnly = true)
    public ProductOrderHistoryPageTO searchProductOrders(String productReference, Long customerId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<ProductOrder> result = productOrderRepository.findAll(
                ProductOrderHistorySpecifications.from(productReference, customerId),
                pageable);
        return new ProductOrderHistoryPageTO(
                result.getContent().stream().map(this::mapProductRow).toList(),
                result.getTotalElements(),
                result.getNumber(),
                result.getSize());
    }

    @Transactional(readOnly = true)
    public MaterialOrderHistoryPageTO searchMaterialOrders(
            String materialCode, Long materialProviderId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<MaterialOrderLine> result = materialOrderLineRepository.findAll(
                MaterialOrderHistorySpecifications.from(materialCode, materialProviderId),
                pageable);
        return new MaterialOrderHistoryPageTO(
                result.getContent().stream().map(this::mapMaterialRow).toList(),
                result.getTotalElements(),
                result.getNumber(),
                result.getSize());
    }

    private ProductOrderHistoryRowTO mapProductRow(ProductOrder line) {
        ProductOrderHistoryRowTO to = new ProductOrderHistoryRowTO();
        to.setId(line.getId());
        to.setQuantity(line.getQuantity());
        to.setPricePerUnit(line.getPricePerUnit());
        PurchaseOrder po = line.getPurchaseOrder();
        if (po != null) {
            to.setOrderedAt(po.getCreatedAt());
            to.setPurchaseOrderId(po.getId());
            to.setPurchaseOrderCode(po.getCode());
            to.setOrderStatus(po.getOrderStatus());
            to.setCurrency(po.getCurrency());
            Customer customer = po.getCustomer();
            if (customer != null) {
                to.setCustomerId(customer.getId());
                to.setCustomerName(customer.getCompanyName());
                to.setBuyerId(customer.getBuyerId());
            }
        }
        Product product = line.getProduct();
        if (product != null) {
            to.setProductId(product.getId());
            to.setProductReference(product.getReference());
            to.setProductName(product.getName());
        }
        return to;
    }

    private MaterialOrderHistoryRowTO mapMaterialRow(MaterialOrderLine line) {
        MaterialOrderHistoryRowTO to = new MaterialOrderHistoryRowTO();
        to.setId(line.getId());
        to.setQuantity(line.getQuantity());
        to.setUnitOfMeasure(line.getUnitOfMeasure());
        MaterialOrder order = line.getMaterialOrder();
        if (order != null) {
            to.setOrderedAt(order.getCreatedAt());
            to.setMaterialOrderId(order.getId());
            to.setMaterialOrderCode(order.getCode());
            to.setStatus(order.getStatus());
            MaterialProvider provider = order.getMaterialProvider();
            if (provider != null) {
                to.setMaterialProviderId(provider.getId());
                to.setMaterialProviderName(provider.getName());
            }
        }
        Material material = line.getMaterial();
        if (material != null) {
            to.setMaterialId(material.getId());
            to.setMaterialCode(material.getCode());
            to.setMaterialName(material.getName());
        }
        return to;
    }
}
