package com.hampcode.pagoya.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecurringBillPaymentResponse(
    Long id,
    String providerName,
    String billCode,
    BigDecimal amount,
    String frequency,
    Integer dayOfMonth,
    Integer dayOfWeek,
    String status,
    LocalDateTime nextRunAt
) {}
