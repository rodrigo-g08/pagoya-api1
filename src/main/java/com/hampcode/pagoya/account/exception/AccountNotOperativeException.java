package com.hampcode.pagoya.account.exception;

import com.hampcode.pagoya.shared.exception.BusinessRuleException;

public class AccountNotOperativeException extends BusinessRuleException {
    public AccountNotOperativeException() {
        super("la cuenta origen no esta operativa");
    }
}
