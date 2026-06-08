package com.hampcode.pagoya.auth.dto;

import jakarta.validation.constraints.*;

public record RegisterUserRequest(
    @NotBlank(message = "el email es obligatorio")
    @Email(message = "el formato del email no es valido")
    String email,

    @NotBlank(message = "la contrasena es obligatoria")
    @Size(min = 8, message = "la contrasena debe tener al menos 8 caracteres")
    String password,

    @NotBlank(message = "el nombre completo es obligatorio")
    @Size(max = 100)
    String fullName,

    @NotBlank(message = "el DNI es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "el DNI debe tener 8 digitos")
    String dni,

    @Pattern(regexp = "\\d{9}", message = "el telefono debe tener 9 digitos")
    String phone
) {}
