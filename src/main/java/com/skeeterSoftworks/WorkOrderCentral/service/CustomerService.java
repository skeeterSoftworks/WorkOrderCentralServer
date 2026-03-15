package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Customer;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer addCustomer(Customer customer) {
        customer.setId(null);
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Customer customer) throws Exception {
        if (customer.getId() == null || customer.getId() <= 0 || !customerRepository.existsById(customer.getId())) {
            throw new Exception("CUSTOMER_NOT_FOUND");
        }
        return customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) throws Exception {
        if (!customerRepository.existsById(id)) {
            throw new Exception("CUSTOMER_NOT_FOUND");
        }
        customerRepository.deleteById(id);
    }
}
