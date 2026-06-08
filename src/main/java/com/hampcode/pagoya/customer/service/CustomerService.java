package com.hampcode.pagoya.customer.service;

import com.hampcode.pagoya.account.repository.AccountRepository;
import com.hampcode.pagoya.auth.model.User;
import com.hampcode.pagoya.auth.repository.UserRepository;
import com.hampcode.pagoya.customer.dto.CustomerResponse;
import com.hampcode.pagoya.customer.dto.UpdateCustomerRequest;
import com.hampcode.pagoya.customer.mapper.CustomerMapper;
import com.hampcode.pagoya.customer.model.Customer;
import com.hampcode.pagoya.customer.repository.CustomerRepository;
import com.hampcode.pagoya.shared.exception.BusinessRuleException;
import com.hampcode.pagoya.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService implements ICustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return customerRepository.findById(id)
            .map(customerMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("cliente no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable)
            .map(customerMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("usuario no encontrado"));
        return customerRepository.findByUser_Id(user.getId())
            .map(customerMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("perfil no encontrado"));
    }

    @Override
    @Transactional
    public CustomerResponse updateByEmail(String email, UpdateCustomerRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("usuario no encontrado"));
        Customer customer = customerRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("perfil no encontrado"));

        customer.setFullName(request.fullName());
        customer.setPhone(request.phone());
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("cliente no encontrado"));
        softDelete(customer);
    }

    @Override
    @Transactional
    public void deleteMe(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("usuario no encontrado"));
        Customer customer = customerRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("perfil no encontrado"));
        softDelete(customer);
    }

    private void softDelete(Customer customer) {
        if (accountRepository.existsByCustomer_IdAndBalanceGreaterThan(
                customer.getId(), BigDecimal.ZERO)) {
            throw new BusinessRuleException(
                "no puedes darte de baja con cuentas que tienen saldo");
        }
        customerRepository.delete(customer);
    }
}
