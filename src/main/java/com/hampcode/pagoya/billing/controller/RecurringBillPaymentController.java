package com.hampcode.pagoya.billing.controller;

import com.hampcode.pagoya.billing.dto.CreateRecurringBillPaymentRequest;
import com.hampcode.pagoya.billing.dto.RecurringBillPaymentResponse;
import com.hampcode.pagoya.billing.service.IRecurringBillPaymentService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recurring-bill-payments")
@RequiredArgsConstructor
@Tag(name = "Recurring Bill Payments", description = "Pagos recurrentes programados")
public class RecurringBillPaymentController {

    private final IRecurringBillPaymentService recurringService;

    @Operation(summary = "Programar un pago recurrente")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pago recurrente programado"),
        @ApiResponse(responseCode = "400", description = "Datos invalidos, proveedor inactivo o configuracion de frecuencia incompleta"),
        @ApiResponse(responseCode = "404", description = "Cliente o proveedor no encontrado")
    })
    @PostMapping
    public ResponseEntity<RecurringBillPaymentResponse> schedule(
            @Valid @RequestBody CreateRecurringBillPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(recurringService.schedule(request));
    }

    @Operation(summary = "Listar pagos recurrentes de un cliente (paginado)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista paginada de pagos recurrentes"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<PageResponse<RecurringBillPaymentResponse>> findByCustomer(
            @PathVariable Long customerId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(
            PageResponse.from(recurringService.findByCustomer(customerId, pageable)));
    }

    @Operation(summary = "Pausar un pago recurrente activo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pago recurrente pausado"),
        @ApiResponse(responseCode = "400", description = "El pago no esta en estado activo"),
        @ApiResponse(responseCode = "404", description = "Pago recurrente no encontrado")
    })
    @PatchMapping("/{id}/pause")
    public ResponseEntity<RecurringBillPaymentResponse> pause(@PathVariable Long id) {
        return ResponseEntity.ok(recurringService.pause(id));
    }

    @Operation(summary = "Reanudar un pago recurrente pausado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pago recurrente reanudado"),
        @ApiResponse(responseCode = "400", description = "El pago no esta en estado pausado"),
        @ApiResponse(responseCode = "404", description = "Pago recurrente no encontrado")
    })
    @PatchMapping("/{id}/resume")
    public ResponseEntity<RecurringBillPaymentResponse> resume(@PathVariable Long id) {
        return ResponseEntity.ok(recurringService.resume(id));
    }

    @Operation(summary = "Cancelar un pago recurrente")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Pago recurrente cancelado"),
        @ApiResponse(responseCode = "400", description = "El pago ya estaba cancelado"),
        @ApiResponse(responseCode = "404", description = "Pago recurrente no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        recurringService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
