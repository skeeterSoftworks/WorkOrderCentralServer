package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.mapper.ProductMapperService;
import com.skeeterSoftworks.WorkOrderCentral.service.ProductDeleteBlockedException;
import com.skeeterSoftworks.WorkOrderCentral.service.ProductService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ApiErrorTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductQualityInfoUpdateTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ProductTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/for-machine/{machineId}")
    public ResponseEntity<?> listForMachine(@PathVariable Long machineId) {
        try {
            if (machineId == null || machineId <= 0) {
                return ResponseEntity.badRequest().body("INVALID_MACHINE_ID");
            }
            List<Product> list = productService.getProductsForMachine(machineId);
            return ResponseEntity.ok(list.stream().map(productMapperService::mapToTO).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_PRODUCTS_FOR_MACHINE");
        }
    }

    @PutMapping("/{productId}/quality-info-steps")
    public ResponseEntity<?> replaceQualityInfoSteps(
            @PathVariable Long productId,
            @RequestBody ProductQualityInfoUpdateTO body
    ) {
        try {
            if (body == null || body.getMachineId() == null || body.getMachineId() <= 0) {
                return ResponseEntity.badRequest().body("INVALID_MACHINE_ID");
            }
            if (body.getQualityInfoSteps() == null) {
                return ResponseEntity.badRequest().body("QUALITY_INFO_STEPS_BODY_REQUIRED");
            }
            Product updated = productService.replaceQualityInfoStepsForProductOnMachine(
                    productId,
                    body.getMachineId(),
                    body.getQualityInfoSteps()
            );
            return ResponseEntity.ok(productMapperService.mapToTO(updated));
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            if ("PRODUCT_NOT_FOUND".equals(msg) || "INVALID_PRODUCT_ID".equals(msg)) {
                return ResponseEntity.notFound().build();
            }
            if ("PRODUCT_NOT_ON_MACHINE".equals(msg)
                    || "INVALID_MACHINE_ID".equals(msg)
                    || "QUALITY_INFO_STEPS_BODY_REQUIRED".equals(msg)) {
                return ResponseEntity.badRequest().body(msg);
            }
            return ResponseEntity.internalServerError().body("ERROR_UPDATING_QUALITY_INFO_STEPS");
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
            if (productTO.getTechnicalDrawingBase64() == null) {
                productService.getProductById(productTO.getId())
                        .ifPresent(p -> entity.setTechnicalDrawing(p.getTechnicalDrawing()));
            }
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
        } catch (ProductDeleteBlockedException e) {
            log.warn("Product {} delete blocked: {} product_order row(s)", id, e.getProductOrderLineCount());
            return ResponseEntity.status(409).body(new ApiErrorTO(
                    "errorProductDeleteLinkedOrderLines",
                    Map.of("count", e.getProductOrderLineCount())
            ));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if ("PRODUCT_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(404).body("PRODUCT_NOT_FOUND");
            }
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}

