package com.hampcode.pagoya.unit.transfer;

import com.hampcode.pagoya.account.exception.AccountNotOperativeException;
import com.hampcode.pagoya.account.model.Account;
import com.hampcode.pagoya.account.model.AccountStatus;
import com.hampcode.pagoya.account.model.AccountType;
import com.hampcode.pagoya.account.repository.AccountRepository;
import com.hampcode.pagoya.customer.model.Customer;
import com.hampcode.pagoya.customer.service.CurrentCustomerService;
import com.hampcode.pagoya.shared.exception.BusinessRuleException;
import com.hampcode.pagoya.transfer.dto.TransferRequest;
import com.hampcode.pagoya.transfer.exception.InsufficientBalanceException;
import com.hampcode.pagoya.transfer.exception.SameAccountTransferException;
import com.hampcode.pagoya.transfer.mapper.TransferMapper;
import com.hampcode.pagoya.transfer.model.Transfer;
import com.hampcode.pagoya.transfer.model.TransferStatus;
import com.hampcode.pagoya.transfer.model.TransferType;
import com.hampcode.pagoya.transfer.model.ValidationResult;
import com.hampcode.pagoya.transfer.repository.ExternalTransferValidationRepository;
import com.hampcode.pagoya.transfer.repository.TransferRepository;
import com.hampcode.pagoya.transfer.service.ExternalTransferValidator;
import com.hampcode.pagoya.transfer.service.ExternalValidationOutcome;
import com.hampcode.pagoya.transfer.service.TransferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock private TransferRepository transferRepository;
    @Mock private ExternalTransferValidationRepository externalValidationRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransferMapper transferMapper;
    @Mock private org.springframework.web.client.RestTemplate restTemplate;
    @Mock private CurrentCustomerService currentCustomerService;
    @Mock private ExternalTransferValidator externalValidator;
    @InjectMocks private TransferService transferService;

    private Account account(String number, AccountStatus status, String balance, Long customerId) {
        return Account.builder()
            .id(number.hashCode() & 0xffffL).accountNumber(number).status(status)
            .type(AccountType.SAVINGS).balance(new BigDecimal(balance))
            .customer(Customer.builder().id(customerId).build())
            .build();
    }

    private TransferRequest internal(String src, String tgt, String amount) {
        return new TransferRequest(src, TransferType.INTERNAL, tgt,
            null, null, null, new BigDecimal(amount), "PEN");
    }

    private TransferRequest external(String src, String cci, String amount) {
        return new TransferRequest(src, TransferType.EXTERNAL, null,
            "BCP", cci, "Juan Perez", new BigDecimal(amount), "PEN");
    }

    private void ownerIs(Long customerId) {
        when(currentCustomerService.current())
            .thenReturn(Customer.builder().id(customerId).build());
    }

    @Test
    void internalTransfer_debitsSourceAndCreditsTarget() {
        Account source = account("SRC", AccountStatus.ACTIVE, "100.00", 5L);
        Account target = account("TGT", AccountStatus.ACTIVE, "0.00", 9L);
        when(accountRepository.findByAccountNumber("SRC")).thenReturn(Optional.of(source));
        when(accountRepository.findByAccountNumber("TGT")).thenReturn(Optional.of(target));
        ownerIs(5L);
        when(transferRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        transferService.transfer(internal("SRC", "TGT", "30.00"));

        assertThat(source.getBalance()).isEqualByComparingTo("70.00");
        assertThat(target.getBalance()).isEqualByComparingTo("30.00");
        ArgumentCaptor<Transfer> captor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(captor.getValue().getType()).isEqualTo(TransferType.INTERNAL);
    }

    @Test
    void transfer_sourceNotOwnedByCurrentUser_forbidden() {
        Account source = account("SRC", AccountStatus.ACTIVE, "100.00", 5L);
        when(accountRepository.findByAccountNumber("SRC")).thenReturn(Optional.of(source));
        ownerIs(99L);

        assertThatThrownBy(() -> transferService.transfer(internal("SRC", "TGT", "30.00")))
            .isInstanceOf(AccessDeniedException.class);
        verify(transferRepository, never()).save(any());
    }

    @Test
    void transfer_sourceNotActive_throws() {
        Account source = account("SRC", AccountStatus.SUSPENDED, "100.00", 5L);
        when(accountRepository.findByAccountNumber("SRC")).thenReturn(Optional.of(source));
        ownerIs(5L);

        assertThatThrownBy(() -> transferService.transfer(internal("SRC", "TGT", "30.00")))
            .isInstanceOf(AccountNotOperativeException.class);
    }

    @Test
    void transfer_insufficientBalance_throws() {
        Account source = account("SRC", AccountStatus.ACTIVE, "10.00", 5L);
        when(accountRepository.findByAccountNumber("SRC")).thenReturn(Optional.of(source));
        ownerIs(5L);

        assertThatThrownBy(() -> transferService.transfer(internal("SRC", "TGT", "50.00")))
            .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    void internalTransfer_sameAccount_throws() {
        Account source = account("SRC", AccountStatus.ACTIVE, "100.00", 5L);
        when(accountRepository.findByAccountNumber("SRC")).thenReturn(Optional.of(source));
        ownerIs(5L);

        assertThatThrownBy(() -> transferService.transfer(internal("SRC", "SRC", "30.00")))
            .isInstanceOf(SameAccountTransferException.class);
    }

    @Test
    void internalTransfer_missingTarget_throws() {
        Account source = account("SRC", AccountStatus.ACTIVE, "100.00", 5L);
        when(accountRepository.findByAccountNumber("SRC")).thenReturn(Optional.of(source));
        ownerIs(5L);

        assertThatThrownBy(() -> transferService.transfer(internal("SRC", null, "30.00")))
            .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void externalTransfer_approved_debitsAndRecordsValidation() {
        Account source = account("SRC", AccountStatus.ACTIVE, "100.00", 5L);
        when(accountRepository.findByAccountNumber("SRC")).thenReturn(Optional.of(source));
        ownerIs(5L);
        when(externalValidator.validate(any())).thenReturn(
            new ExternalValidationOutcome(ValidationResult.APPROVED, "EXT-ABC", "ok"));
        when(transferRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        transferService.transfer(external("SRC", "00219912345678901234", "40.00"));

        assertThat(source.getBalance()).isEqualByComparingTo("60.00");
        ArgumentCaptor<Transfer> captor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TransferStatus.COMPLETED);
        verify(externalValidationRepository).save(any());
    }

    @Test
    void externalTransfer_rejected_failsWithoutDebiting() {
        Account source = account("SRC", AccountStatus.ACTIVE, "100.00", 5L);
        when(accountRepository.findByAccountNumber("SRC")).thenReturn(Optional.of(source));
        ownerIs(5L);
        when(externalValidator.validate(any())).thenReturn(
            new ExternalValidationOutcome(ValidationResult.REJECTED, "EXT-XYZ", "bad cci"));
        when(transferRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        transferService.transfer(external("SRC", "123", "40.00"));

        assertThat(source.getBalance()).isEqualByComparingTo("100.00");
        ArgumentCaptor<Transfer> captor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TransferStatus.FAILED);
        verify(externalValidationRepository).save(any());
    }

    @Test
    void externalTransfer_missingBankData_throws() {
        Account source = account("SRC", AccountStatus.ACTIVE, "100.00", 5L);
        when(accountRepository.findByAccountNumber("SRC")).thenReturn(Optional.of(source));
        ownerIs(5L);
        TransferRequest request = new TransferRequest("SRC", TransferType.EXTERNAL, null,
            null, null, null, new BigDecimal("40.00"), "PEN");

        assertThatThrownBy(() -> transferService.transfer(request))
            .isInstanceOf(BusinessRuleException.class);
        verify(transferRepository, never()).save(any());
    }
}
