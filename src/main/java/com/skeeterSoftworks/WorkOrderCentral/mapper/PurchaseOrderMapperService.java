package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.*;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.CustomerRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EPurchaseOrderStatus;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderMapperService {

    private final ProductMapperService productMapperService;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Autowired
    public PurchaseOrderMapperService(ProductMapperService productMapperService,
                                      CustomerRepository customerRepository,
                                      ProductRepository productRepository) {
        this.productMapperService = productMapperService;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    public PurchaseOrderTO mapToTO(PurchaseOrder po) {
        if (po == null) return null;

        PurchaseOrderTO to = new PurchaseOrderTO();
        to.setId(po.getId());
        to.setCustomer(mapCustomerToTO(po.getCustomer()));
        to.setProductOrderList(mapProductOrderListToTO(po.getProductOrderList()));
        to.setOrderStatus(po.getOrderStatus());
        to.setCurrency(po.getCurrency());
        to.setDeliveryDate(po.getDeliveryDate());
        to.setReference(po.getReference());
        to.setDeliveryTerms(po.getDeliveryTerms());
        to.setShippingAddress(po.getShippingAddress());
        to.setComment(po.getComment());
        to.setCreatedAt(po.getCreatedAt());
        to.setConfirmedAt(po.getConfirmedAt());
        to.setInProductionAt(po.getInProductionAt());
        to.setCompletedAt(po.getCompletedAt());
        to.setDeliveredAt(po.getDeliveredAt());
        return to;
    }

    public PurchaseOrder mapToEntity(PurchaseOrderTO to) {
        if (to == null) return null;

        PurchaseOrder po = new PurchaseOrder();
        if (to.getId() != null) po.setId(to.getId());
        po.setCustomer(resolveCustomer(to));
        po.setProductOrderList(mapProductOrderListToEntity(to.getProductOrderList()));
        // For new orders (no id), default status to CREATED
        if (to.getId() == null) {
            po.setOrderStatus(EPurchaseOrderStatus.CREATED);
        } else {
            po.setOrderStatus(to.getOrderStatus());
        }
        po.setCurrency(to.getCurrency());
        po.setDeliveryDate(to.getDeliveryDate());
        po.setReference(to.getReference());
        po.setDeliveryTerms(to.getDeliveryTerms());
        po.setShippingAddress(to.getShippingAddress());
        po.setComment(to.getComment());
        if (to.getCreatedAt() != null) po.setCreatedAt(to.getCreatedAt());
        if (to.getConfirmedAt() != null) po.setConfirmedAt(to.getConfirmedAt());
        if (to.getInProductionAt() != null) po.setInProductionAt(to.getInProductionAt());
        if (to.getCompletedAt() != null) po.setCompletedAt(to.getCompletedAt());
        if (to.getDeliveredAt() != null) po.setDeliveredAt(to.getDeliveredAt());
        return po;
    }

    public CustomerTO mapCustomerToTO(Customer c) {
        if (c == null) return null;
        CustomerTO to = new CustomerTO();
        to.setId(c.getId());
        to.setCompanyName(c.getCompanyName());
        to.setAddressData(c.getAddressData());
        to.setDescription(c.getDescription());
        return to;
    }

    private Customer resolveCustomer(PurchaseOrderTO to) {
        Long id = null;
        if (to.getCustomer() != null && to.getCustomer().getId() != null) {
            id = to.getCustomer().getId();
        } else if (to.getCustomerId() != null) {
            id = to.getCustomerId();
        }
        if (id != null) {
            return customerRepository.findById(id).orElse(null);
        }
        return mapCustomerToEntity(to.getCustomer());
    }

    public Customer mapCustomerToEntity(CustomerTO to) {
        if (to == null) return null;
        if (to.getId() != null) {
            return customerRepository.findById(to.getId()).orElse(null);
        }
        Customer c = new Customer();
        c.setCompanyName(to.getCompanyName());
        c.setAddressData(to.getAddressData());
        c.setDescription(to.getDescription());
        return c;
    }

    public ProductOrderTO mapProductOrderToTO(ProductOrder po) {
        if (po == null) return null;
        ProductOrderTO to = new ProductOrderTO();
        to.setId(po.getId());
        to.setProduct(mapProductToTO(po.getProduct()));
        to.setQuantity(po.getQuantity());
        to.setPricePerUnit(po.getPricePerUnit());
        return to;
    }

    public ProductOrder mapProductOrderToEntity(ProductOrderTO to) {
        if (to == null) return null;
        ProductOrder po = new ProductOrder();
        if (to.getId() != null) po.setId(to.getId());
        if (to.getProduct() != null && to.getProduct().getId() != null) {
            productRepository.findById(to.getProduct().getId()).ifPresent(po::setProduct);
        }
        po.setQuantity(to.getQuantity());
        po.setPricePerUnit(to.getPricePerUnit());
        return po;
    }

    public ProductTO mapProductToTO(Product p) {
        return productMapperService.mapToTO(p);
    }

    public Product mapProductToEntity(ProductTO to) {
        return productMapperService.mapToEntity(to);
    }

    public List<ProductOrderTO> mapProductOrderListToTO(List<ProductOrder> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(this::mapProductOrderToTO).collect(Collectors.toList());
    }

    public List<ProductOrder> mapProductOrderListToEntity(List<ProductOrderTO> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(this::mapProductOrderToEntity).collect(Collectors.toList());
    }

}

