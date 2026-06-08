package com.hampcode.pagoya.account.dto;

import com.hampcode.pagoya.account.model.AccountType;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(
    @NotNull(message = "el tipo de cuenta es obligatorio")
    AccountType type
) {}
