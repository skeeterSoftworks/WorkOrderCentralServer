package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Customer;
import com.skeeterSoftworks.WorkOrderCentral.mapper.CustomerMapperService;
import com.skeeterSoftworks.WorkOrderCentral.service.CustomerService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.CustomerTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/customers")
@CrossOrigin(origins = "*")
public class CustomerFacade {

    private final CustomerService customerService;
    private final CustomerMapperService customerMapperService;

    @Autowired
    public CustomerFacade(CustomerService customerService, CustomerMapperService customerMapperService) {
        this.customerService = customerService;
        this.customerMapperService = customerMapperService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            List<Customer> all = customerService.getAllCustomers();
            return ResponseEntity.ok(all.stream().map(customerMapperService::mapToTO).toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_CUSTOMERS");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return customerService.getCustomerById(id)
                    .map(customerMapperService::mapToTO)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_FETCHING_CUSTOMER");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody CustomerTO customerTO) {
        log.debug("Facade call: addCustomer");

        try {
            Customer entity = customerMapperService.mapToEntity(customerTO);
            Customer saved = customerService.addCustomer(entity);
            return ResponseEntity.ok(customerMapperService.mapToTO(saved));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ERROR_SAVING_CUSTOMER");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody CustomerTO customerTO) {
        log.debug("Facade call: updateCustomer");

        if (customerTO.getId() == null || customerTO.getId() <= 0) {
            log.error("Invalid id for update: {}", customerTO.getId());
            return ResponseEntity.badRequest().body("INVALID_ID");
        }

        try {
            Customer entity = customerMapperService.mapToEntity(customerTO);
            Customer updated = customerService.updateCustomer(entity);
            return ResponseEntity.ok(customerMapperService.mapToTO(updated));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
