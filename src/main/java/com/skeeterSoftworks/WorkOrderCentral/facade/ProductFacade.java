package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.mapper.ProductMapperService;
import com.skeeterSoftworks.WorkOrderCentral.service.ProductService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*")
public class ProductFacade {

    private final ProductService productService;
    private final ProductMapperService productMapperService;

    @Autowired
    public ProductFacade(ProductService productService, ProductMapperService productMapperService) {
        this.productService = productService;
        this.productMapperService = productMapperService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            List<Product> all = productService.getAllProducts();
            return ResponseEntity.ok(all.stream().map(productMapperService::mapToTO).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_PRODUCTS");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return productService.getProductById(id)
                    .map(productMapperService::mapToTO)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_PRODUCT");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody ProductTO productTO) {
        log.debug("Facade call: addProduct");

        try {
            Product entity = productMapperService.mapToEntity(productTO);
            Product saved = productService.addProduct(entity);
            return ResponseEntity.ok(productMapperService.mapToTO(saved));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_SAVING_PRODUCT");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody ProductTO productTO) {
        log.debug("Facade call: updateProduct");

        if (productTO.getId() == null || productTO.getId() <= 0) {
            log.error("Invalid id for update: {}", productTO.getId());
            return ResponseEntity.badRequest().body("INVALID_ID");
        }

        try {
            Product entity = productMapperService.mapToEntity(productTO);
            Product updated = productService.updateProduct(entity);
            return ResponseEntity.ok(productMapperService.mapToTO(updated));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}

