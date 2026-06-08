package com.hampcode.pagoya.transfer.controller;

import com.hampcode.pagoya.transfer.dto.TransferByCurrencyReport;
import com.hampcode.pagoya.transfer.dto.TransferByDayReport;
import com.hampcode.pagoya.transfer.dto.TransferByStatusReport;
import com.hampcode.pagoya.transfer.service.ITransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transfers/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Transfer Reports", description = "Reportes agregados de transferencias")
public class TransferReportController {

    private final ITransferService transferService;

    @Operation(summary = "Total transferido por moneda")
    @GetMapping("/by-currency")
    public List<TransferByCurrencyReport> byCurrency() {
        return transferService.reportByCurrency();
    }

    @Operation(summary = "Transferencias por dia (rango from-to)")
    @GetMapping("/by-day")
    public List<TransferByDayReport> byDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return transferService.reportByDay(from, to);
    }

    @Operation(summary = "Distribucion por estado")
    @GetMapping("/by-status")
    public List<TransferByStatusReport> byStatus() {
        return transferService.reportByStatus();
    }
}
