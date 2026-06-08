package com.hampcode.pagoya.billing.mapper;

import com.hampcode.pagoya.billing.dto.RecurringBillPaymentResponse;
import com.hampcode.pagoya.billing.model.RecurringBillPayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecurringBillPaymentMapper {

    @Mapping(target = "providerName", source = "provider.name")
    @Mapping(target = "frequency", expression = "java(r.getFrequency().name())")
    @Mapping(target = "status", expression = "java(r.getStatus().name())")
    RecurringBillPaymentResponse toResponse(RecurringBillPayment r);
}
