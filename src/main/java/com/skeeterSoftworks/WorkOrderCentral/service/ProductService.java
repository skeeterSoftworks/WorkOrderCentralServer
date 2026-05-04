package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Customer;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Product;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.QualityInfoStep;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.ProductRepository;
import com.skeeterSoftworks.WorkOrderCentral.mapper.ProductMapperService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.QualityInfoStepTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductOrderRepository productOrderRepository;
    private final ProductMapperService productMapperService;
    private final CustomerService customerService;

    @Autowired
    public ProductService(
            ProductRepository productRepository,
            ProductOrderRepository productOrderRepository,
            ProductMapperService productMapperService,
            CustomerService customerService
    ) {
        this.productRepository = productRepository;
        this.productOrderRepository = productOrderRepository;
        this.productMapperService = productMapperService;
        this.customerService = customerService;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsForMachine(Long machineId) {
        if (machineId == null || machineId <= 0) {
            return List.of();
        }
        return productRepository.findByMachines_Id(machineId);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product addProduct(Product product) {
        product.setId(null);
        Customer internal = customerService.ensureInternalStockOrdererCustomerExists();
        attachInternalStockOrdererIfMissing(product, internal);
        return productRepository.save(product);
    }

    public Product updateProduct(Product product) throws Exception {
        if (product.getId() == null || product.getId() <= 0 || !productRepository.existsById(product.getId())) {
            throw new Exception("PRODUCT_NOT_FOUND");
        }
        Customer internal = customerService.ensureInternalStockOrdererCustomerExists();
        attachInternalStockOrdererIfMissing(product, internal);
        return productRepository.save(product);
    }

    private static void attachInternalStockOrdererIfMissing(Product product, Customer internal) {
        if (internal == null || internal.getId() == null) {
            return;
        }
        if (product.getCustomers() == null) {
            product.setCustomers(new ArrayList<>());
        }
        boolean linked = product.getCustomers().stream()
                .anyMatch(c -> c.getId() != null && c.getId().equals(internal.getId()));
        if (!linked) {
            product.getCustomers().add(internal);
        }
    }

    @Transactional
    public void deleteProduct(Long id) throws Exception {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            throw new Exception("PRODUCT_NOT_FOUND");
        }
        long orderLines = productOrderRepository.countByProduct_Id(id);
        if (orderLines > 0) {
            int count = orderLines > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) orderLines;
            throw new ProductDeleteBlockedException(count);
        }
        productRepository.delete(product);
    }

    @Transactional
    public Product replaceQualityInfoStepsForProductOnMachine(
            Long productId,
            Long machineId,
            List<QualityInfoStepTO> stepTos
    ) throws Exception {
        if (productId == null || productId <= 0) {
            throw new Exception("INVALID_PRODUCT_ID");
        }
        if (machineId == null || machineId <= 0) {
            throw new Exception("INVALID_MACHINE_ID");
        }
        if (stepTos == null) {
            throw new Exception("QUALITY_INFO_STEPS_BODY_REQUIRED");
        }
        Product product = productRepository.findById(productId).orElseThrow(() -> new Exception("PRODUCT_NOT_FOUND"));
        if (product.getMachines() != null) {
            product.getMachines().size();
        }
        boolean onMachine = product.getMachines() != null
                && product.getMachines().stream().anyMatch(m -> machineId.equals(m.getId()));
        if (!onMachine) {
            throw new Exception("PRODUCT_NOT_ON_MACHINE");
        }
        if (product.getQualityInfoSteps() != null) {
            product.getQualityInfoSteps().clear();
        } else {
            product.setQualityInfoSteps(new ArrayList<>());
        }
        int n = 1;
        for (QualityInfoStepTO to : stepTos) {
            if (to == null) {
                continue;
            }
            QualityInfoStep entity = productMapperService.mapQualityStepTOToEntity(to);
            entity.setStepNumber(n++);
            entity.setProduct(product);
            product.getQualityInfoSteps().add(entity);
        }
        Customer internal = customerService.ensureInternalStockOrdererCustomerExists();
        attachInternalStockOrdererIfMissing(product, internal);
        return productRepository.save(product);
    }
}

