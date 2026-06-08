package com.hampcode.pagoya.account.service;

import com.hampcode.pagoya.account.dto.AccountBalanceResponse;
import com.hampcode.pagoya.account.dto.AccountResponse;
import com.hampcode.pagoya.account.dto.AccountSummaryReport;
import com.hampcode.pagoya.account.dto.CreateAccountRequest;
import com.hampcode.pagoya.account.dto.DepositRequest;
import com.hampcode.pagoya.account.dto.RecipientAccountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface IAccountService {
    AccountResponse create(CreateAccountRequest request);
    AccountBalanceResponse getBalance(String accountNumber);
    AccountBalanceResponse deposit(String accountNumber, DepositRequest request);
    AccountResponse close(String accountNumber);
    Page<AccountResponse> findByCustomer(Long customerId, Pageable pageable);
    Page<AccountResponse> findMyAccounts(Pageable pageable);
    List<RecipientAccountResponse> findRecipientAccountsByDni(String dni);
    List<AccountSummaryReport> reportSummary();
}
