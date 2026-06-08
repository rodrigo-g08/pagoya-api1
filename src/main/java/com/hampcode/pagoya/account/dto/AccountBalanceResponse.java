package com.hampcode.pagoya.account.dto;

import java.math.BigDecimal;

public record AccountBalanceResponse(
    String accountNumber,
    BigDecimal balance
) {}
