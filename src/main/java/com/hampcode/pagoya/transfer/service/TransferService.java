package com.hampcode.pagoya.transfer.service;

import com.hampcode.pagoya.account.exception.AccountNotOperativeException;
import com.hampcode.pagoya.account.model.Account;
import com.hampcode.pagoya.account.model.AccountStatus;
import com.hampcode.pagoya.account.repository.AccountRepository;
import com.hampcode.pagoya.shared.exception.BusinessRuleException;
import com.hampcode.pagoya.shared.exception.ResourceNotFoundException;
import com.hampcode.pagoya.transfer.dto.TransferByCurrencyReport;
import com.hampcode.pagoya.transfer.dto.TransferByDayReport;
import com.hampcode.pagoya.transfer.dto.TransferByStatusReport;
import com.hampcode.pagoya.transfer.dto.TransferRequest;
import com.hampcode.pagoya.transfer.dto.TransferResponse;
import com.hampcode.pagoya.transfer.exception.InsufficientBalanceException;
import com.hampcode.pagoya.transfer.exception.SameAccountTransferException;
import com.hampcode.pagoya.transfer.mapper.TransferMapper;
import com.hampcode.pagoya.transfer.model.ExternalTransferValidation;
import com.hampcode.pagoya.transfer.model.Transfer;
import com.hampcode.pagoya.transfer.model.TransferStatus;
import com.hampcode.pagoya.transfer.model.TransferType;
import com.hampcode.pagoya.transfer.repository.ExternalTransferValidationRepository;
import com.hampcode.pagoya.transfer.repository.TransferRepository;
import com.hampcode.pagoya.customer.service.CurrentCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransferService implements ITransferService {

    private final TransferRepository transferRepository;
    private final ExternalTransferValidationRepository externalValidationRepository;
    private final AccountRepository accountRepository;
    private final TransferMapper transferMapper;
    private final RestTemplate restTemplate;
    private final CurrentCustomerService currentCustomerService;
    private final ExternalTransferValidator externalValidator;

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        Account source = accountRepository
            .findByAccountNumber(request.sourceAccountNumber())
            .orElseThrow(() -> new ResourceNotFoundException(
                "cuenta origen no encontrada"));

        
        Long currentCustomerId = currentCustomerService.current().getId();
        if (!currentCustomerId.equals(source.getCustomer().getId())) {
            throw new AccessDeniedException("no tienes permiso para esta operacion");
        }
        
        if (source.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotOperativeException();
        }
        
        if (source.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException();
        }

        BigDecimal rate = null;
        BigDecimal creditAmount = request.amount();
        if (!"PEN".equals(request.currency())) {
            rate = getExchangeRate(request.currency());
            creditAmount = request.amount().multiply(rate);
        }

        Transfer.TransferBuilder transfer = Transfer.builder()
            .type(request.type())
            .sourceAccount(source)
            .amount(request.amount())
            .currency(request.currency())
            .exchangeRate(rate)
            .createdAt(LocalDateTime.now());

        if (request.type() == TransferType.INTERNAL) {
            Account target = resolveInternalTarget(request);
            source.setBalance(source.getBalance().subtract(request.amount()));
            target.setBalance(target.getBalance().add(creditAmount));
            accountRepository.save(source);
            accountRepository.save(target);
            transfer.targetAccount(target).status(TransferStatus.COMPLETED);
            return transferMapper.toResponse(transferRepository.save(transfer.build()));
        }

        return processExternal(request, source, transfer);
    }

    private TransferResponse processExternal(TransferRequest request, Account source,
                                             Transfer.TransferBuilder transfer) {
        validateExternalData(request);
        ExternalValidationOutcome outcome = externalValidator.validate(request);

        if (outcome.approved()) {
            source.setBalance(source.getBalance().subtract(request.amount()));
            accountRepository.save(source);
        }

        transfer
            .externalBankName(request.externalBankName())
            .externalAccountNumber(request.externalAccountNumber())
            .externalBeneficiaryName(request.externalBeneficiaryName())
            .status(outcome.approved() ? TransferStatus.COMPLETED : TransferStatus.FAILED);

        Transfer saved = transferRepository.save(transfer.build());
        externalValidationRepository.save(ExternalTransferValidation.builder()
            .transfer(saved)
            .trackingCode(outcome.trackingCode())
            .result(outcome.result())
            .reason(outcome.reason())
            .validatedAt(LocalDateTime.now())
            .build());
        return transferMapper.toResponse(saved);
    }

    private Account resolveInternalTarget(TransferRequest request) {
        if (isBlank(request.targetAccountNumber())) {
            throw new BusinessRuleException(
                "la cuenta destino es obligatoria para transferencias internas");
        }
        
        if (request.sourceAccountNumber().equals(request.targetAccountNumber())) {
            throw new SameAccountTransferException();
        }
        return accountRepository.findByAccountNumber(request.targetAccountNumber())
            .orElseThrow(() -> new ResourceNotFoundException(
                "cuenta destino no encontrada"));
    }

    private void validateExternalData(TransferRequest request) {
        if (isBlank(request.externalBankName())
                || isBlank(request.externalAccountNumber())
                || isBlank(request.externalBeneficiaryName())) {
            throw new BusinessRuleException(
                "los datos del banco destino son obligatorios para transferencias externas");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferResponse> findByAccountNumber(
            String accountNumber, Pageable pageable) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new ResourceNotFoundException(
                "cuenta no encontrada"));
        currentCustomerService.assertCanAccessCustomer(account.getCustomer().getId());
        return transferRepository
            .findBySourceAccount_Id(account.getId(), pageable)
            .map(transferMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferByCurrencyReport> reportByCurrency() {
        return transferRepository.reportByCurrencyRaw().stream()
            .map(r -> new TransferByCurrencyReport(
                (String) r[0],
                ((Number) r[1]).longValue(),
                (BigDecimal) r[2]))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferByDayReport> reportByDay(LocalDate from, LocalDate to) {
        return transferRepository.reportByDayRaw(from, to).stream()
            .map(r -> new TransferByDayReport(
                toLocalDate(r[0]),
                ((Number) r[1]).longValue(),
                (BigDecimal) r[2]))
            .toList();
    }

 
    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferByStatusReport> reportByStatus() {
        return transferRepository.reportByStatusRaw().stream()
            .map(r -> new TransferByStatusReport(
                (String) r[0],
                ((Number) r[1]).longValue()))
            .toList();
    }

    private BigDecimal getExchangeRate(String currency) {
        String url = "https://api.frankfurter.dev/v2/rates?base=PEN&quotes=" + currency;
        Map response = restTemplate.getForObject(url, Map.class);
        Map rates = (Map) response.get("rates");
        return new BigDecimal(rates.get(currency).toString());
    }
}
