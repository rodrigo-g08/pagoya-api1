package com.hampcode.pagoya.billing.exception;

import com.hampcode.pagoya.shared.exception.BusinessRuleException;

public class InvalidStatusTransitionException extends BusinessRuleException {
    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
