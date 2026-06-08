package com.hampcode.pagoya.billing.exception;

import com.hampcode.pagoya.shared.exception.BusinessRuleException;

public class InactiveProviderException extends BusinessRuleException {
    public InactiveProviderException() {
        super("el proveedor seleccionado no esta disponible");
    }
}
