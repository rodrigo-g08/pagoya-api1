package com.hampcode.pagoya.billing.exception;

import com.hampcode.pagoya.shared.exception.BusinessRuleException;

public class DuplicateBillPaymentException extends BusinessRuleException {
    public DuplicateBillPaymentException() {
        super("ya tienes registrado un pago para este recibo");
    }
}
