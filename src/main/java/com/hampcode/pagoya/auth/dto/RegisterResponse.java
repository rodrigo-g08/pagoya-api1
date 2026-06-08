package com.hampcode.pagoya.auth.dto;

public record RegisterResponse(
    Long userId,
    String email,
    String role,
    Long customerId,
    String fullName,
    String dni
) {}
