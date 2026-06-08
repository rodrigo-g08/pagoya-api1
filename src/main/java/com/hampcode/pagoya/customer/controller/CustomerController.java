package com.hampcode.pagoya.customer.controller;

import com.hampcode.pagoya.customer.dto.CustomerResponse;
import com.hampcode.pagoya.customer.dto.UpdateCustomerRequest;
import com.hampcode.pagoya.customer.service.ICustomerService;
import com.hampcode.pagoya.shared.pagination.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Perfiles de cliente de PagoYa")
public class CustomerController {

    private final ICustomerService customerService;

    @Operation(summary = "Obtener mi perfil")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil del usuario autenticado"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> findMe(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(customerService.findByEmail(email));
    }

    @Operation(summary = "Actualizar mi perfil (fullName, phone)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil actualizado"),
        @ApiResponse(responseCode = "400", description = "Datos invalidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PutMapping("/me")
    public ResponseEntity<CustomerResponse> updateMe(
            Authentication authentication,
            @Valid @RequestBody UpdateCustomerRequest request) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(customerService.updateByEmail(email, request));
    }

    @Operation(summary = "Darme de baja (soft delete de mi propio perfil)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Perfil dado de baja"),
        @ApiResponse(responseCode = "400", description = "Tienes cuentas con saldo"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        customerService.deleteMe(email);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener un cliente por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.findById(id));
    }

    @Operation(summary = "Listar clientes paginados")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista paginada de clientes")
    })
    @GetMapping
    public ResponseEntity<PageResponse<CustomerResponse>> findAll(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(
            PageResponse.from(customerService.findAll(pageable)));
    }

    @Operation(summary = "Eliminar un cliente (solo ADMIN)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Cliente eliminado"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permiso")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
