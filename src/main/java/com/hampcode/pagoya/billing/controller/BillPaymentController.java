package com.hampcode.pagoya.billing.controller;

import com.hampcode.pagoya.billing.dto.BillPaymentResponse;
import com.hampcode.pagoya.billing.dto.CreateBillPaymentRequest;
import com.hampcode.pagoya.billing.dto.PaymentByCategoryResponse;
import com.hampcode.pagoya.billing.service.IBillPaymentService;
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

import java.util.List;

@RestController
@RequestMapping("/api/bill-payments")
@RequiredArgsConstructor
@Tag(name = "Bill Payments", description = "Pagos de servicios del cliente")
public class BillPaymentController {

    private final IBillPaymentService billPaymentService;

    @Operation(summary = "Registrar un pago de servicio")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pago registrado"),
        @ApiResponse(responseCode = "400", description = "Datos invalidos, proveedor inactivo o pago duplicado"),
        @ApiResponse(responseCode = "404", description = "Cliente o proveedor no encontrado")
    })
    @PostMapping
    public ResponseEntity<BillPaymentResponse> pay(
            @Valid @RequestBody CreateBillPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(billPaymentService.pay(request));
    }

    @Operation(summary = "Listar pagos de un cliente (paginado)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista paginada de pagos"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<PageResponse<BillPaymentResponse>> findByCustomer(
            @PathVariable Long customerId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(
            PageResponse.from(billPaymentService.findByCustomer(customerId, pageable)));
    }

    @Operation(summary = "Reporte de pagos del cliente agrupados por categoria")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reporte por categoria"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/customer/{customerId}/by-category")
    public ResponseEntity<List<PaymentByCategoryResponse>> reportByCategory(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(billPaymentService.reportByCategory(customerId));
    }
}
