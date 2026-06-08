package com.hampcode.pagoya.auth.exception;

import com.hampcode.pagoya.shared.exception.BusinessRuleException;

public class InvalidRefreshTokenException extends BusinessRuleException {
    public InvalidRefreshTokenException() {
        super("refresh token invalido, expirado o revocado");
    }
}
