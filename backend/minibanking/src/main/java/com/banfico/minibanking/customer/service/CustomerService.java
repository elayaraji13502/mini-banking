package com.banfico.minibanking.customer.service;

import com.banfico.minibanking.customer.dto.CustomerRequest;
import com.banfico.minibanking.customer.dto.CustomerResponse;
import com.banfico.minibanking.customer.entity.Customer;
import com.banfico.minibanking.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.banfico.minibanking.exception.DuplicateResourceException;
import com.banfico.minibanking.exception.ResourceNotFoundException;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Customer with email already exists"
            );
        }

        Customer customer = new Customer(
                request.getName(),
                request.getEmail(),
                request.getPhone()
        );

        Customer savedCustomer = customerRepository.save(customer);

        return CustomerResponse.fromEntity(savedCustomer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {

        return customerRepository.findAll()
                .stream()
                .map(CustomerResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + id
                        )
                );

        return CustomerResponse.fromEntity(customer);
    }

    @Transactional
    public CustomerResponse updateCustomer(
            Long id,
            CustomerRequest request
    ) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Customer not found with id: " + id
                        )
                );

        if (!customer.getEmail().equals(request.getEmail())
                && customerRepository.existsByEmail(request.getEmail())) {

            throw new DuplicateResourceException(
                    "Another customer already uses this email"
            );
        }

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());

        Customer updatedCustomer = customerRepository.save(customer);

        return CustomerResponse.fromEntity(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long id) {

        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Customer not found with id: " + id
            );
        }

        customerRepository.deleteById(id);
    }
}