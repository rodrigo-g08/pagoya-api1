package com.hampcode.pagoya.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DepositRequest(
    @NotNull(message = "el monto es obligatorio")
    @DecimalMin(value = "1.00", message = "el monto minimo de recarga es S/. 1.00")
    BigDecimal amount
) {}
