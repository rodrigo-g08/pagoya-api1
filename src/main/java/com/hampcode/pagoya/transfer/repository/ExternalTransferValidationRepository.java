package com.hampcode.pagoya.transfer.repository;

import com.hampcode.pagoya.transfer.model.ExternalTransferValidation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalTransferValidationRepository
        extends JpaRepository<ExternalTransferValidation, Long> {
}
