package com.hampcode.pagoya.billing.exception;

import com.hampcode.pagoya.shared.exception.BusinessRuleException;

public class InvalidRecurringScheduleException extends BusinessRuleException {
    public InvalidRecurringScheduleException(String message) {
        super(message);
    }
}
