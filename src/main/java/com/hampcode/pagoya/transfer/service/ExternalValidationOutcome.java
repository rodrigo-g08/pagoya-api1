package com.hampcode.pagoya.transfer.service;

import com.hampcode.pagoya.transfer.model.ValidationResult;

public record ExternalValidationOutcome(
    ValidationResult result,
    String trackingCode,
    String reason
) {
    public boolean approved() {
        return result == ValidationResult.APPROVED;
    }
}
