package com.hampcode.pagoya.customer.service;

import com.hampcode.pagoya.auth.model.User;
import com.hampcode.pagoya.auth.repository.UserRepository;
import com.hampcode.pagoya.customer.model.Customer;
import com.hampcode.pagoya.customer.repository.CustomerRepository;
import com.hampcode.pagoya.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentCustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public Customer current() {
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("usuario no encontrado"));
        return customerRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("perfil no encontrado"));
    }

    public boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch("ROLE_ADMIN"::equals);
    }

    
    @Transactional(readOnly = true)
    public void assertCanAccessCustomer(Long ownerCustomerId) {
        if (isAdmin()) {
            return;
        }
        if (!current().getId().equals(ownerCustomerId)) {
            throw new AccessDeniedException("no tienes permiso para esta operacion");
        }
    }
}
