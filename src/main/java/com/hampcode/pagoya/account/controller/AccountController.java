package com.hampcode.pagoya.account.controller;

import com.hampcode.pagoya.account.dto.AccountBalanceResponse;
import com.hampcode.pagoya.account.dto.AccountResponse;
import com.hampcode.pagoya.account.dto.CreateAccountRequest;
import com.hampcode.pagoya.account.dto.DepositRequest;
import com.hampcode.pagoya.account.dto.RecipientAccountResponse;
import com.hampcode.pagoya.account.service.IAccountService;
import com.hampcode.pagoya.shared.pagination.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Cuentas de los clientes")
public class AccountController {

    private final IAccountService accountService;

    @Operation(summary = "Abrir una cuenta para el usuario autenticado")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cuenta creada"),
        @ApiResponse(responseCode = "400", description = "Datos invalidos o cuenta duplicada"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping
    public ResponseEntity<AccountResponse> create(
            @Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(accountService.create(request));
    }

    @Operation(summary = "Consultar el saldo de una cuenta")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Saldo entregado"),
        @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<AccountBalanceResponse> getBalance(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getBalance(accountNumber));
    }

    @Operation(summary = "Recargar saldo a una cuenta (dueño o ADMIN)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Saldo actualizado"),
        @ApiResponse(responseCode = "400", description = "Monto invalido o cuenta no operativa"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permiso"),
        @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<AccountBalanceResponse> deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(accountService.deposit(accountNumber, request));
    }

    @Operation(summary = "Cerrar una cuenta (dueño o ADMIN, saldo en cero)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cuenta cerrada"),
        @ApiResponse(responseCode = "400", description = "La cuenta tiene saldo o ya esta cerrada"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permiso"),
        @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @PatchMapping("/{accountNumber}/close")
    public ResponseEntity<AccountResponse> close(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.close(accountNumber));
    }

    @Operation(summary = "Listar mis cuentas (del usuario autenticado, paginado)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista paginada de mis cuentas"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/me")
    public ResponseEntity<PageResponse<AccountResponse>> findMyAccounts(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(
            PageResponse.from(accountService.findMyAccounts(pageable)));
    }

    @Operation(summary = "Buscar cuentas destino de un cliente por DNI (para transferir)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cuentas activas del destinatario"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/recipient")
    public ResponseEntity<List<RecipientAccountResponse>> findRecipientByDni(
            @RequestParam String dni) {
        return ResponseEntity.ok(accountService.findRecipientAccountsByDni(dni));
    }

    @Operation(summary = "Listar cuentas de un cliente por id (solo ADMIN)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista paginada de cuentas"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permiso")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<PageResponse<AccountResponse>> findByCustomer(
            @PathVariable Long customerId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(
            PageResponse.from(accountService.findByCustomer(customerId, pageable)));
    }
}
