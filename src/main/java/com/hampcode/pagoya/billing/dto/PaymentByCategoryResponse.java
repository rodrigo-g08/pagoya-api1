package com.hampcode.pagoya.billing.dto;

import java.math.BigDecimal;

public record PaymentByCategoryResponse(
    String category,
    long totalCount,
    BigDecimal totalAmount
) {}
