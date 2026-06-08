package com.hampcode.pagoya.unit.transfer;

import com.hampcode.pagoya.transfer.dto.TransferRequest;
import com.hampcode.pagoya.transfer.model.TransferType;
import com.hampcode.pagoya.transfer.model.ValidationResult;
import com.hampcode.pagoya.transfer.service.ExternalTransferValidator;
import com.hampcode.pagoya.transfer.service.ExternalValidationOutcome;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalTransferValidatorTest {

    private final ExternalTransferValidator validator = new ExternalTransferValidator();

    private TransferRequest externalTo(String cci) {
        return new TransferRequest("ACC-SOURCE", TransferType.EXTERNAL, null,
            "BCP", cci, "Juan Perez", new BigDecimal("50.00"), "PEN");
    }

    @Test
    void validate_valid20DigitCci_approved() {
        ExternalValidationOutcome outcome = validator.validate(
            externalTo("00219912345678901234"));

        assertThat(outcome.result()).isEqualTo(ValidationResult.APPROVED);
        assertThat(outcome.approved()).isTrue();
        assertThat(outcome.trackingCode()).startsWith("EXT-");
    }

    @Test
    void validate_invalidCci_rejected() {
        ExternalValidationOutcome outcome = validator.validate(externalTo("123"));

        assertThat(outcome.result()).isEqualTo(ValidationResult.REJECTED);
        assertThat(outcome.approved()).isFalse();
        assertThat(outcome.reason()).contains("20 digitos");
    }
}
