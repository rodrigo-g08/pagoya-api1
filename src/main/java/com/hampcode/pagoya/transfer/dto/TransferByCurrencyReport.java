package com.hampcode.pagoya.transfer.dto;

import java.math.BigDecimal;

public record TransferByCurrencyReport(
    String currency,
    Long totalTransfers,
    BigDecimal totalAmount
) {}
