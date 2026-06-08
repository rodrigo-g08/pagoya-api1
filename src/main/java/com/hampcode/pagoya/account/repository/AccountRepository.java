package com.hampcode.pagoya.account.repository;

import com.hampcode.pagoya.account.model.Account;
import com.hampcode.pagoya.account.model.AccountStatus;
import com.hampcode.pagoya.account.model.AccountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByCustomer_IdAndType(Long customerId, AccountType type);
    Optional<Account> findByAccountNumber(String accountNumber);
    Page<Account> findByCustomer_Id(Long customerId, Pageable pageable);
    List<Account> findByCustomer_IdAndStatus(Long customerId, AccountStatus status);
    boolean existsByCustomer_IdAndBalanceGreaterThan(Long customerId, BigDecimal balance);

    @Query(value = "SELECT * FROM fn_account_report_summary()",
           nativeQuery = true)
    List<Object[]> reportSummaryRaw();
}
