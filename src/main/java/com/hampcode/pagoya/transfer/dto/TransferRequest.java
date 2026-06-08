package com.hampcode.pagoya.transfer.dto;

import com.hampcode.pagoya.transfer.model.TransferType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TransferRequest(
    @NotBlank(message = "la cuenta origen es obligatoria")
    String sourceAccountNumber,

    @NotNull(message = "el tipo de transferencia es obligatorio")
    TransferType type,

    String targetAccountNumber,

    String externalBankName,

    String externalAccountNumber,

    String externalBeneficiaryName,

    @NotNull(message = "el monto es obligatorio")
    @DecimalMin(value = "1.00", message = "el monto minimo es S/. 1.00")
    BigDecimal amount,

    @NotBlank(message = "la moneda es obligatoria")
    @Pattern(regexp = "PEN|USD|EUR|GBP",
             message = "la moneda debe ser PEN, USD, EUR o GBP")
    String currency
) {}
