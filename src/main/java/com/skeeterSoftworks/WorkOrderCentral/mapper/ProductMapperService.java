package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductTO;
import org.springframework.stereotype.Service;

@Service
public class ProductMapperService {

    public ProductTO mapToTO(Product product) {
        if (product == null) return null;
        ProductTO to = new ProductTO();
        to.setId(product.getId());
        to.setName(product.getName());
        to.setDescription(product.getDescription());
        to.setMachineType(product.getMachineType());
        to.setToolType(product.getToolType());
        return to;
    }

    public Product mapToEntity(ProductTO to) {
        if (to == null) return null;
        Product product = new Product();
        if (to.getId() != null) {
            product.setId(to.getId());
        }
        product.setName(to.getName());
        product.setDescription(to.getDescription());
        product.setMachineType(to.getMachineType());
        product.setToolType(to.getToolType());
        return product;
    }
}

