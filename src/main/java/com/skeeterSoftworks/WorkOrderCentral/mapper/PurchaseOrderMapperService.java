package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.*;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EPurchaseOrderStatus;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderMapperService {

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
        return to;
    }

    public PurchaseOrder mapToEntity(PurchaseOrderTO to) {
        if (to == null) return null;

        PurchaseOrder po = new PurchaseOrder();
        if (to.getId() != null) po.setId(to.getId());
        po.setCustomer(mapCustomerToEntity(to.getCustomer()));
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

    public Customer mapCustomerToEntity(CustomerTO to) {
        if (to == null) return null;
        Customer c = new Customer();
        if (to.getId() != null) c.setId(to.getId());
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
        po.setProduct(mapProductToEntity(to.getProduct()));
        po.setQuantity(to.getQuantity());
        po.setPricePerUnit(to.getPricePerUnit());
        return po;
    }

    public ProductTO mapProductToTO(Product p) {
        if (p == null) return null;
        ProductTO to = new ProductTO();
        to.setId(p.getId());
        to.setName(p.getName());
        to.setDescription(p.getDescription());
        return to;
    }

    public Product mapProductToEntity(ProductTO to) {
        if (to == null) return null;
        Product p = new Product();
        if (to.getId() != null) p.setId(to.getId());
        p.setName(to.getName());
        p.setDescription(to.getDescription());
        return p;
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

