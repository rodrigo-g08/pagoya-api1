package com.hampcode.pagoya.account.service;

import com.hampcode.pagoya.account.dto.AccountBalanceResponse;
import com.hampcode.pagoya.account.dto.AccountResponse;
import com.hampcode.pagoya.account.dto.AccountSummaryReport;
import com.hampcode.pagoya.account.dto.CreateAccountRequest;
import com.hampcode.pagoya.account.dto.DepositRequest;
import com.hampcode.pagoya.account.dto.RecipientAccountResponse;
import com.hampcode.pagoya.account.exception.AccountNotOperativeException;
import com.hampcode.pagoya.account.exception.DuplicateAccountTypeException;
import com.hampcode.pagoya.account.mapper.AccountMapper;
import com.hampcode.pagoya.account.model.Account;
import com.hampcode.pagoya.account.model.AccountStatus;
import com.hampcode.pagoya.account.repository.AccountRepository;
import com.hampcode.pagoya.customer.model.Customer;
import com.hampcode.pagoya.customer.repository.CustomerRepository;
import com.hampcode.pagoya.customer.service.CurrentCustomerService;
import com.hampcode.pagoya.shared.exception.BusinessRuleException;
import com.hampcode.pagoya.shared.exception.ResourceNotFoundException;
import com.hampcode.pagoya.shared.util.MaskUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final CurrentCustomerService currentCustomerService;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        Customer customer = currentCustomerService.current();
        if (accountRepository.existsByCustomer_IdAndType(
                customer.getId(), request.type()))
            throw new DuplicateAccountTypeException();
        Account account = Account.builder()
            .accountNumber(UUID.randomUUID().toString().substring(0, 12).toUpperCase())
            .balance(BigDecimal.ZERO)
            .status(AccountStatus.ACTIVE)
            .type(request.type())
            .customer(customer)
            .build();
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountBalanceResponse getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new ResourceNotFoundException("cuenta no encontrada"));
        currentCustomerService.assertCanAccessCustomer(account.getCustomer().getId());
        return accountMapper.toBalance(account);
    }

    @Override
    @Transactional
    public AccountBalanceResponse deposit(String accountNumber, DepositRequest request) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new ResourceNotFoundException("cuenta no encontrada"));
        currentCustomerService.assertCanAccessCustomer(account.getCustomer().getId());
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotOperativeException();
        }
        account.setBalance(account.getBalance().add(request.amount()));
        return accountMapper.toBalance(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountResponse close(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new ResourceNotFoundException("cuenta no encontrada"));
        currentCustomerService.assertCanAccessCustomer(account.getCustomer().getId());
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessRuleException("la cuenta ya esta cerrada");
        }
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessRuleException(
                "no puedes cerrar una cuenta con saldo; transfiere o retira primero");
        }
        account.setStatus(AccountStatus.CLOSED);
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponse> findByCustomer(Long customerId, Pageable pageable) {
        return accountRepository.findByCustomer_Id(customerId, pageable)
            .map(accountMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponse> findMyAccounts(Pageable pageable) {
        Long customerId = currentCustomerService.current().getId();
        return accountRepository.findByCustomer_Id(customerId, pageable)
            .map(accountMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipientAccountResponse> findRecipientAccountsByDni(String dni) {
        Customer recipient = customerRepository.findByDni(dni)
            .orElseThrow(() -> new ResourceNotFoundException("cliente no encontrado"));
        String maskedName = MaskUtil.maskName(recipient.getFullName());
        return accountRepository
            .findByCustomer_IdAndStatus(recipient.getId(), AccountStatus.ACTIVE)
            .stream()
            .map(account -> new RecipientAccountResponse(
                account.getAccountNumber(), account.getType(), maskedName))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountSummaryReport> reportSummary() {
        return accountRepository.reportSummaryRaw().stream()
            .map(r -> new AccountSummaryReport(
                (String) r[0],
                (String) r[1],
                ((Number) r[2]).longValue(),
                (BigDecimal) r[3]))
            .toList();
    }
}
