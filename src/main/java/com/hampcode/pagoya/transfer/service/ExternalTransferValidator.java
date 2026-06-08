package com.hampcode.pagoya.transfer.service;

import com.hampcode.pagoya.transfer.dto.TransferRequest;
import com.hampcode.pagoya.transfer.model.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
public class ExternalTransferValidator {

    public ExternalValidationOutcome validate(TransferRequest request) {
        String trackingCode = "EXT-" + UUID.randomUUID().toString()
            .substring(0, 8).toUpperCase();
        String cci = request.externalAccountNumber();

        if (cci != null && cci.matches("\\d{20}")) {
            return new ExternalValidationOutcome(
                ValidationResult.APPROVED, trackingCode, "validacion aprobada");
        }
        return new ExternalValidationOutcome(
            ValidationResult.REJECTED, trackingCode,
            "el CCI debe tener 20 digitos");
    }
}
