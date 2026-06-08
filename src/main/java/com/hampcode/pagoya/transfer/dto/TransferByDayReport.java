package com.hampcode.pagoya.transfer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransferByDayReport(
    LocalDate day,
    Long totalTransfers,
    BigDecimal totalAmount
) {}
