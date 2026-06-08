package com.hampcode.pagoya.customer.dto;

public record CustomerResponse(
    Long id,
    String fullName,
    String dni,
    String phone,
    Long userId
) {}
