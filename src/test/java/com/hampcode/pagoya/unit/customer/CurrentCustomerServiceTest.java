package com.hampcode.pagoya.unit.customer;

import com.hampcode.pagoya.auth.model.User;
import com.hampcode.pagoya.auth.repository.UserRepository;
import com.hampcode.pagoya.customer.model.Customer;
import com.hampcode.pagoya.customer.repository.CustomerRepository;
import com.hampcode.pagoya.customer.service.CurrentCustomerService;
import com.hampcode.pagoya.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentCustomerServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CustomerRepository customerRepository;
    @InjectMocks private CurrentCustomerService currentCustomerService;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String email, String role) {
        var auth = new UsernamePasswordAuthenticationToken(
            email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void current_resolvesCustomerFromAuthenticatedEmail() {
        authenticateAs("ana@pagoya.com", "CUSTOMER");
        User user = User.builder().id(7L).email("ana@pagoya.com").build();
        Customer customer = Customer.builder().id(3L).user(user).build();
        when(userRepository.findByEmail("ana@pagoya.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(7L)).thenReturn(Optional.of(customer));

        assertThat(currentCustomerService.current().getId()).isEqualTo(3L);
    }

    @Test
    void current_userNotFound_throws() {
        authenticateAs("ghost@pagoya.com", "CUSTOMER");
        when(userRepository.findByEmail("ghost@pagoya.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currentCustomerService.current())
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void isAdmin_reflectsAuthorities() {
        authenticateAs("boss@pagoya.com", "ADMIN");
        assertThat(currentCustomerService.isAdmin()).isTrue();

        SecurityContextHolder.clearContext();
        authenticateAs("ana@pagoya.com", "CUSTOMER");
        assertThat(currentCustomerService.isAdmin()).isFalse();
    }

    @Test
    void assertCanAccessCustomer_adminAlwaysPasses() {
        authenticateAs("boss@pagoya.com", "ADMIN");
        currentCustomerService.assertCanAccessCustomer(999L);
    }

    @Test
    void assertCanAccessCustomer_ownerPasses() {
        authenticateAs("ana@pagoya.com", "CUSTOMER");
        User user = User.builder().id(7L).email("ana@pagoya.com").build();
        Customer customer = Customer.builder().id(3L).user(user).build();
        when(userRepository.findByEmail("ana@pagoya.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(7L)).thenReturn(Optional.of(customer));

        currentCustomerService.assertCanAccessCustomer(3L);
    }

    @Test
    void assertCanAccessCustomer_otherCustomerDenied() {
        authenticateAs("ana@pagoya.com", "CUSTOMER");
        User user = User.builder().id(7L).email("ana@pagoya.com").build();
        Customer customer = Customer.builder().id(3L).user(user).build();
        when(userRepository.findByEmail("ana@pagoya.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(7L)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> currentCustomerService.assertCanAccessCustomer(99L))
            .isInstanceOf(AccessDeniedException.class);
    }
}
