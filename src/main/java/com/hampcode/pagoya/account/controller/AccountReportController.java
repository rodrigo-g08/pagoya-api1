package com.hampcode.pagoya.account.controller;

import com.hampcode.pagoya.account.dto.AccountSummaryReport;
import com.hampcode.pagoya.account.service.IAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Account Reports", description = "Reportes agregados de cuentas")
public class AccountReportController {

    private final IAccountService accountService;

    @Operation(summary = "Resumen de cuentas por tipo y estado")
    @GetMapping("/summary")
    public List<AccountSummaryReport> summary() {
        return accountService.reportSummary();
    }
}
