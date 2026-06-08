package com.hampcode.pagoya.customer.service;

import com.hampcode.pagoya.customer.dto.CustomerResponse;
import com.hampcode.pagoya.customer.dto.UpdateCustomerRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICustomerService {
    CustomerResponse findById(Long id);
    Page<CustomerResponse> findAll(Pageable pageable);
    CustomerResponse findByEmail(String email);
    CustomerResponse updateByEmail(String email, UpdateCustomerRequest request);
    void delete(Long id);
    void deleteMe(String email);
}
