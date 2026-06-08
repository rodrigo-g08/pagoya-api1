package com.hampcode.pagoya.auth.controller;

import com.hampcode.pagoya.auth.dto.AuthResponse;
import com.hampcode.pagoya.auth.dto.LoginRequest;
import com.hampcode.pagoya.auth.dto.RefreshRequest;
import com.hampcode.pagoya.auth.dto.RegisterResponse;
import com.hampcode.pagoya.auth.dto.RegisterUserRequest;
import com.hampcode.pagoya.auth.service.IAuthService;
import com.hampcode.pagoya.auth.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registro, login, refresh, logout y logout-all")
public class AuthController {

    private final IUserService userService;
    private final IAuthService authService;

    @Operation(summary = "Registro: crea credenciales (User) y perfil (Customer) atomicamente")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuario y perfil creados"),
        @ApiResponse(responseCode = "400", description = "Email ya registrado, DNI duplicado o datos invalidos")
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @Operation(summary = "Login: emite access token (JWT) y refresh token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tokens emitidos"),
        @ApiResponse(responseCode = "401", description = "Credenciales invalidas")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Renovar tokens (rotation): emite nuevo access y nuevo refresh")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tokens rotados"),
        @ApiResponse(responseCode = "400", description = "Refresh token invalido, expirado o revocado")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @Operation(summary = "Cerrar sesion en este dispositivo: revoca el refresh token")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Sesion cerrada")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cerrar sesion en TODOS los dispositivos del usuario autenticado")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Todas las sesiones cerradas"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(Authentication authentication) {
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        authService.logoutAll(principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}
