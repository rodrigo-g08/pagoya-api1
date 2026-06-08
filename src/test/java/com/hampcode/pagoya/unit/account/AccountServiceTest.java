package com.hampcode.pagoya.unit.account;

import com.hampcode.pagoya.account.dto.AccountBalanceResponse;
import com.hampcode.pagoya.account.dto.CreateAccountRequest;
import com.hampcode.pagoya.account.dto.DepositRequest;
import com.hampcode.pagoya.account.dto.RecipientAccountResponse;
import com.hampcode.pagoya.account.exception.AccountNotOperativeException;
import com.hampcode.pagoya.account.exception.DuplicateAccountTypeException;
import com.hampcode.pagoya.account.mapper.AccountMapper;
import com.hampcode.pagoya.account.model.Account;
import com.hampcode.pagoya.account.model.AccountStatus;
import com.hampcode.pagoya.account.model.AccountType;
import com.hampcode.pagoya.account.repository.AccountRepository;
import com.hampcode.pagoya.customer.model.Customer;
import com.hampcode.pagoya.customer.repository.CustomerRepository;
import com.hampcode.pagoya.customer.service.CurrentCustomerService;
import com.hampcode.pagoya.shared.exception.BusinessRuleException;
import com.hampcode.pagoya.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private AccountMapper accountMapper;
    @Mock private CurrentCustomerService currentCustomerService;
    @Mock private CustomerRepository customerRepository;
    @InjectMocks private com.hampcode.pagoya.account.service.AccountService accountService;

    private Account account(String number, AccountStatus status, BigDecimal balance, Long customerId) {
        return Account.builder()
            .id(1L).accountNumber(number).status(status).type(AccountType.SAVINGS)
            .balance(balance).customer(Customer.builder().id(customerId).build())
            .build();
    }

    @Test
    void create_duplicateType_throws() {
        when(currentCustomerService.current()).thenReturn(Customer.builder().id(10L).build());
        when(accountRepository.existsByCustomer_IdAndType(10L, AccountType.SAVINGS))
            .thenReturn(true);

        assertThatThrownBy(() -> accountService.create(
                new CreateAccountRequest(AccountType.SAVINGS)))
            .isInstanceOf(DuplicateAccountTypeException.class);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void create_persistsAccountForCurrentCustomer() {
        when(currentCustomerService.current()).thenReturn(Customer.builder().id(10L).build());
        when(accountRepository.existsByCustomer_IdAndType(10L, AccountType.SAVINGS))
            .thenReturn(false);
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        accountService.create(new CreateAccountRequest(AccountType.SAVINGS));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getCustomer().getId()).isEqualTo(10L);
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(captor.getValue().getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void deposit_addsBalanceToActiveAccount() {
        Account acc = account("ACC1", AccountStatus.ACTIVE, new BigDecimal("100.00"), 5L);
        when(accountRepository.findByAccountNumber("ACC1")).thenReturn(Optional.of(acc));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountMapper.toBalance(any()))
            .thenReturn(new AccountBalanceResponse("ACC1", new BigDecimal("150.00")));

        accountService.deposit("ACC1", new DepositRequest(new BigDecimal("50.00")));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo("150.00");
    }

    @Test
    void deposit_notActive_throws() {
        Account acc = account("ACC1", AccountStatus.SUSPENDED, new BigDecimal("100.00"), 5L);
        when(accountRepository.findByAccountNumber("ACC1")).thenReturn(Optional.of(acc));

        assertThatThrownBy(() -> accountService.deposit(
                "ACC1", new DepositRequest(new BigDecimal("50.00"))))
            .isInstanceOf(AccountNotOperativeException.class);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void getBalance_ownershipDenied_propagates() {
        Account acc = account("ACC1", AccountStatus.ACTIVE, new BigDecimal("100.00"), 5L);
        when(accountRepository.findByAccountNumber("ACC1")).thenReturn(Optional.of(acc));
        doThrow(new AccessDeniedException("denied"))
            .when(currentCustomerService).assertCanAccessCustomer(5L);

        assertThatThrownBy(() -> accountService.getBalance("ACC1"))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void findRecipientAccountsByDni_masksNameAndExposesNoBalance() {
        Customer recipient = Customer.builder().id(5L).fullName("Juan Perez").build();
        when(customerRepository.findByDni("12345678")).thenReturn(Optional.of(recipient));
        when(accountRepository.findByCustomer_IdAndStatus(5L, AccountStatus.ACTIVE))
            .thenReturn(List.of(account("ACC1", AccountStatus.ACTIVE, new BigDecimal("999"), 5L)));

        List<RecipientAccountResponse> result =
            accountService.findRecipientAccountsByDni("12345678");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).ownerName()).isEqualTo("J*** P***");
        assertThat(result.get(0).accountNumber()).isEqualTo("ACC1");
    }

    @Test
    void close_zeroBalance_setsClosed() {
        Account acc = account("ACC1", AccountStatus.ACTIVE, BigDecimal.ZERO, 5L);
        when(accountRepository.findByAccountNumber("ACC1")).thenReturn(Optional.of(acc));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountMapper.toResponse(any())).thenReturn(null);

        accountService.close("ACC1");

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AccountStatus.CLOSED);
    }

    @Test
    void close_withBalance_throws() {
        Account acc = account("ACC1", AccountStatus.ACTIVE, new BigDecimal("10.00"), 5L);
        when(accountRepository.findByAccountNumber("ACC1")).thenReturn(Optional.of(acc));

        assertThatThrownBy(() -> accountService.close("ACC1"))
            .isInstanceOf(BusinessRuleException.class);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void close_alreadyClosed_throws() {
        Account acc = account("ACC1", AccountStatus.CLOSED, BigDecimal.ZERO, 5L);
        when(accountRepository.findByAccountNumber("ACC1")).thenReturn(Optional.of(acc));

        assertThatThrownBy(() -> accountService.close("ACC1"))
            .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void findRecipientAccountsByDni_notFound_throws() {
        when(customerRepository.findByDni("0")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findRecipientAccountsByDni("0"))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
