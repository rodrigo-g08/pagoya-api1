package com.hampcode.pagoya.unit.customer;

import com.hampcode.pagoya.account.repository.AccountRepository;
import com.hampcode.pagoya.auth.model.User;
import com.hampcode.pagoya.auth.repository.UserRepository;
import com.hampcode.pagoya.customer.mapper.CustomerMapper;
import com.hampcode.pagoya.customer.model.Customer;
import com.hampcode.pagoya.customer.repository.CustomerRepository;
import com.hampcode.pagoya.customer.service.CustomerService;
import com.hampcode.pagoya.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private CustomerMapper customerMapper;
    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @InjectMocks private CustomerService customerService;

    private void currentCustomer() {
        User user = User.builder().id(7L).email("ana@pagoya.com").build();
        Customer customer = Customer.builder().id(3L).user(user).build();
        when(userRepository.findByEmail("ana@pagoya.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(7L)).thenReturn(Optional.of(customer));
    }

    @Test
    void deleteMe_withAccountsWithBalance_throws() {
        currentCustomer();
        when(accountRepository.existsByCustomer_IdAndBalanceGreaterThan(3L, BigDecimal.ZERO))
            .thenReturn(true);

        assertThatThrownBy(() -> customerService.deleteMe("ana@pagoya.com"))
            .isInstanceOf(BusinessRuleException.class);
        verify(customerRepository, never()).delete(any());
    }

    @Test
    void deleteMe_noBalance_softDeletes() {
        currentCustomer();
        when(accountRepository.existsByCustomer_IdAndBalanceGreaterThan(3L, BigDecimal.ZERO))
            .thenReturn(false);

        customerService.deleteMe("ana@pagoya.com");

        verify(customerRepository).delete(argThat(c -> c.getId().equals(3L)));
    }
}
