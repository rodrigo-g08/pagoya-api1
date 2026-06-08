package com.hampcode.pagoya.auth.exception;

import com.hampcode.pagoya.shared.exception.BusinessRuleException;

public class EmailAlreadyExistsException extends BusinessRuleException {
    public EmailAlreadyExistsException() {
        super("el email ingresado ya esta registrado");
    }
}
