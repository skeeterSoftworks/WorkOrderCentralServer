package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Customer;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.CustomerTO;
import org.springframework.stereotype.Service;

@Service
public class CustomerMapperService {

    public CustomerTO mapToTO(Customer customer) {
        if (customer == null) return null;
        CustomerTO to = new CustomerTO();
        to.setId(customer.getId());
        to.setCompanyName(customer.getCompanyName());
        to.setBuyerId(customer.getBuyerId());
        to.setAddressData(customer.getAddressData());
        to.setDescription(customer.getDescription());
        return to;
    }

    public Customer mapToEntity(CustomerTO to) {
        if (to == null) return null;
        Customer customer = new Customer();
        if (to.getId() != null) {
            customer.setId(to.getId());
        }
        customer.setCompanyName(to.getCompanyName());
        customer.setBuyerId(blankToNull(to.getBuyerId()));
        customer.setAddressData(to.getAddressData());
        customer.setDescription(to.getDescription());
        return customer;
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
