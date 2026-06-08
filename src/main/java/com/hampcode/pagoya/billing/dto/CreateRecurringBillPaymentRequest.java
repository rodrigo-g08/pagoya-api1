package com.hampcode.pagoya.billing.dto;

import com.hampcode.pagoya.billing.model.RecurringFrequency;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateRecurringBillPaymentRequest(
    @NotNull(message = "el customerId es obligatorio")
    Long customerId,

    @NotNull(message = "el providerId es obligatorio")
    Long providerId,

    @NotBlank(message = "el codigo del recibo es obligatorio")
    @Size(max = 50)
    String billCode,

    @NotNull(message = "el monto es obligatorio")
    @DecimalMin(value = "0.01", message = "el monto debe ser mayor a 0")
    @DecimalMax(value = "5000.00", message = "el monto no puede superar 5000")
    BigDecimal amount,

    @NotNull(message = "la frecuencia es obligatoria")
    RecurringFrequency frequency,

    @Min(value = 1, message = "el dia del mes debe estar entre 1 y 28")
    @Max(value = 28, message = "el dia del mes debe estar entre 1 y 28")
    Integer dayOfMonth,

    @Min(value = 1, message = "el dia de la semana debe estar entre 1 (lunes) y 7 (domingo)")
    @Max(value = 7, message = "el dia de la semana debe estar entre 1 (lunes) y 7 (domingo)")
    Integer dayOfWeek
) {}
