package com.hampcode.pagoya.customer.exception;

import com.hampcode.pagoya.shared.exception.BusinessRuleException;

public class DniAlreadyExistsException extends BusinessRuleException {
    public DniAlreadyExistsException() {
        super("el DNI ingresado ya esta registrado");
    }
}
