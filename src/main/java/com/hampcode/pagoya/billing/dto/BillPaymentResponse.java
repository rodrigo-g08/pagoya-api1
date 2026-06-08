package com.hampcode.pagoya.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BillPaymentResponse(
    Long id,
    String providerName,
    String billCode,
    BigDecimal amount,
    String status,
    LocalDateTime paidAt
) {}
