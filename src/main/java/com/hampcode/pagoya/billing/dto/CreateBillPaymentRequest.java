package com.hampcode.pagoya.billing.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateBillPaymentRequest(
    @NotNull(message = "el customerId es obligatorio")
    Long customerId,

    @NotNull(message = "el providerId es obligatorio")
    Long providerId,

    @NotBlank(message = "el codigo del recibo es obligatorio")
    @Size(max = 50, message = "el codigo del recibo no puede exceder 50 caracteres")
    String billCode,

    @NotNull(message = "el monto es obligatorio")
    @DecimalMin(value = "0.01", message = "el monto debe ser mayor a 0")
    @DecimalMax(value = "5000.00", message = "el monto no puede superar 5000")
    BigDecimal amount
) {}
