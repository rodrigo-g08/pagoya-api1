package com.hampcode.pagoya.transfer.exception;

import com.hampcode.pagoya.shared.exception.BusinessRuleException;

public class SameAccountTransferException extends BusinessRuleException {
    public SameAccountTransferException() {
        super("la cuenta origen y destino no pueden ser la misma");
    }
}
