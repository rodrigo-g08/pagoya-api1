package com.hampcode.pagoya.transfer.controller;

import com.hampcode.pagoya.shared.pagination.PageResponse;
import com.hampcode.pagoya.transfer.dto.TransferRequest;
import com.hampcode.pagoya.transfer.dto.TransferResponse;
import com.hampcode.pagoya.transfer.service.ITransferService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Transferencias entre cuentas")
public class TransferController {

    private final ITransferService transferService;

    @Operation(summary = "Realizar una transferencia entre cuentas")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Transferencia ejecutada"),
        @ApiResponse(responseCode = "400", description = "Datos invalidos, saldo insuficiente o cuentas iguales"),
        @ApiResponse(responseCode = "404", description = "Cuenta origen o destino no encontrada")
    })
    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(transferService.transfer(request));
    }

    @Operation(summary = "Listar transferencias por cuenta origen (paginado)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista paginada de transferencias")
    })
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<PageResponse<TransferResponse>> findByAccountNumber(
            @PathVariable String accountNumber,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(
            transferService.findByAccountNumber(accountNumber, pageable)));
    }
}
