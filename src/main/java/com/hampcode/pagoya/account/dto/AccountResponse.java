package com.hampcode.pagoya.account.dto;

import com.hampcode.pagoya.account.model.AccountStatus;
import com.hampcode.pagoya.account.model.AccountType;
import java.math.BigDecimal;

public record AccountResponse(
    Long id,
    String accountNumber,
    BigDecimal balance,
    AccountStatus status,
    AccountType type,
    Long customerId
) {}
