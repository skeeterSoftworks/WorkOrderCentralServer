package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.InternalStockOrdererConstants;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Customer;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Customer existing = customerRepository.findById(id).orElse(null);
        if (existing != null && isInternalStockOrdererCustomer(existing)) {
            throw new Exception("INTERNAL_STOCK_ORDERER_DELETE_FORBIDDEN");
        }
        customerRepository.deleteById(id);
    }

    private static boolean isInternalStockOrdererCustomer(Customer c) {
        String n = c.getCompanyName() == null ? "" : c.getCompanyName().trim();
        return InternalStockOrdererConstants.COMPANY_NAME.equals(n)
                || InternalStockOrdererConstants.LEGACY_COMPANY_NAME_EN.equals(n);
    }

    /**
     * Returns a managed {@link Customer} row for internal stock POs, creating or renaming from legacy English name as needed.
     */
    @Transactional
    public Customer ensureInternalStockOrdererCustomerExists() {
        Optional<Customer> sr = customerRepository.findFirstByCompanyName(InternalStockOrdererConstants.COMPANY_NAME);
        if (sr.isPresent()) {
            return sr.get();
        }
        Optional<Customer> legacy = customerRepository.findFirstByCompanyName(InternalStockOrdererConstants.LEGACY_COMPANY_NAME_EN);
        if (legacy.isPresent()) {
            Customer c = legacy.get();
            c.setCompanyName(InternalStockOrdererConstants.COMPANY_NAME);
            return customerRepository.save(c);
        }
        Customer c = new Customer();
        c.setCompanyName(InternalStockOrdererConstants.COMPANY_NAME);
        c.setAddressData("—");
        c.setDescription("Sistemski kupac za interne narudžbenice (magacin / internalStockDemand).");
        return customerRepository.save(c);
    }
}
