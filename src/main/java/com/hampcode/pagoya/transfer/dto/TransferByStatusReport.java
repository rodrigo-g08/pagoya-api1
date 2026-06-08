package com.hampcode.pagoya.transfer.dto;

public record TransferByStatusReport(
    String status,
    Long total
) {}
