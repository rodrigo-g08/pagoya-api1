package com.hampcode.pagoya.billing.controller;

import com.hampcode.pagoya.billing.dto.ServiceProviderResponse;
import com.hampcode.pagoya.billing.service.IServiceProviderService;
import com.hampcode.pagoya.shared.pagination.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/service-providers")
@RequiredArgsConstructor
@Tag(name = "Service Providers", description = "Catalogo de proveedores de servicios")
public class ServiceProviderController {

    private final IServiceProviderService providerService;

    @Operation(summary = "Listar proveedores activos (paginado)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista paginada de proveedores activos")
    })
    @GetMapping
    public ResponseEntity<PageResponse<ServiceProviderResponse>> findAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(
            PageResponse.from(providerService.findAllActive(pageable)));
    }
}
