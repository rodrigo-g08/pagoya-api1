package com.hampcode.pagoya.account.exception;

import com.hampcode.pagoya.shared.exception.BusinessRuleException;

public class DuplicateAccountTypeException extends BusinessRuleException {
    public DuplicateAccountTypeException() {
        super("ya tiene una cuenta de este tipo");
    }
}
