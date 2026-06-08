package com.hampcode.pagoya.transfer.service;

import com.hampcode.pagoya.transfer.dto.TransferByCurrencyReport;
import com.hampcode.pagoya.transfer.dto.TransferByDayReport;
import com.hampcode.pagoya.transfer.dto.TransferByStatusReport;
import com.hampcode.pagoya.transfer.dto.TransferRequest;
import com.hampcode.pagoya.transfer.dto.TransferResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ITransferService {
    TransferResponse transfer(TransferRequest request);
    Page<TransferResponse> findByAccountNumber(String accountNumber, Pageable pageable);
    List<TransferByCurrencyReport> reportByCurrency();
    List<TransferByDayReport> reportByDay(LocalDate from, LocalDate to);
    List<TransferByStatusReport> reportByStatus();
}
