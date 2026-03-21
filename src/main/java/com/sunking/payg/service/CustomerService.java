package com.sunking.payg.service;

import com.sunking.payg.dto.CustomerDTO;
import com.sunking.payg.exception.ResourceAlreadyExistsException;
import com.sunking.payg.exception.ResourceNotFoundException;
import com.sunking.payg.mapper.CustomerMapper;
import com.sunking.payg.model.Customer;
import com.sunking.payg.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional
    public CustomerDTO.Response createCustomer(CustomerDTO.CreateRequest request) {
        log.info("Creating customer with email: {}", request.getEmail());

        // Check if customer already exists
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Customer with email " + request.getEmail() + " already exists");
        }

        if (customerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ResourceAlreadyExistsException("Customer with phone number " + request.getPhoneNumber() + " already exists");
        }

        Customer customer = customerMapper.toEntity(request);
        Customer savedCustomer = customerRepository.save(customer);

        log.info("Customer created successfully with ID: {}", savedCustomer.getId());
        return customerMapper.toResponse(savedCustomer);
    }

    @Transactional(readOnly = true)
    public CustomerDTO.Response getCustomerById(Long id) {
        log.info("Fetching customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
        return customerMapper.toResponse(customer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerDTO.Response> getAllCustomers(Pageable pageable) {
        log.info("Fetching all customers with pagination: {}", pageable);
        return customerRepository.findAll(pageable)
                .map(customerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CustomerDTO.Response> searchCustomers(String searchTerm, Pageable pageable) {
        log.info("Searching customers with term: {}", searchTerm);
        return customerRepository.searchCustomers(searchTerm, pageable)
                .map(customerMapper::toResponse);
    }

    @Transactional
    public CustomerDTO.Response updateCustomer(Long id, CustomerDTO.UpdateRequest request) {
        log.info("Updating customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

        customerMapper.updateEntity(customer, request);
        Customer updatedCustomer = customerRepository.save(customer);

        log.info("Customer updated successfully with ID: {}", updatedCustomer.getId());
        return customerMapper.toResponse(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with ID: {}", id);
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found with ID: " + id);
        }
        customerRepository.deleteById(id);
        log.info("Customer deleted successfully with ID: {}", id);
    }
}
