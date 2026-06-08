package com.hampcode.pagoya.transfer.dto;

import com.hampcode.pagoya.transfer.model.TransferStatus;
import com.hampcode.pagoya.transfer.model.TransferType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
    Long id,
    TransferType type,
    String sourceAccountNumber,
    String targetAccountNumber,
    String externalBankName,
    String externalAccountNumber,
    String externalBeneficiaryName,
    BigDecimal amount,
    String currency,
    BigDecimal exchangeRate,
    TransferStatus status,
    LocalDateTime createdAt
) {}
