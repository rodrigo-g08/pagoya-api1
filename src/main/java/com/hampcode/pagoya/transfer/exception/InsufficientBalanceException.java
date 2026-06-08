package com.hampcode.pagoya.transfer.exception;

import com.hampcode.pagoya.shared.exception.BusinessRuleException;

public class InsufficientBalanceException extends BusinessRuleException {
    public InsufficientBalanceException() { super("saldo insuficiente"); }
}
