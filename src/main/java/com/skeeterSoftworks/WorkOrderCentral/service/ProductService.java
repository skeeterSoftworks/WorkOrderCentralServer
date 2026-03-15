package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product addProduct(Product product) {
        product.setId(0L);
        return productRepository.save(product);
    }

    public Product updateProduct(Product product) throws Exception {
        if (product.getId() <= 0 || !productRepository.existsById(product.getId())) {
            throw new Exception("PRODUCT_NOT_FOUND");
        }
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) throws Exception {
        if (!productRepository.existsById(id)) {
            throw new Exception("PRODUCT_NOT_FOUND");
        }
        productRepository.deleteById(id);
    }
}

