package com.hampcode.pagoya.billing.mapper;

import com.hampcode.pagoya.billing.dto.BillPaymentResponse;
import com.hampcode.pagoya.billing.dto.CreateBillPaymentRequest;
import com.hampcode.pagoya.billing.model.BillPayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BillPaymentMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "customer",  ignore = true)
    @Mapping(target = "provider",  ignore = true)
    @Mapping(target = "status",    ignore = true)
    @Mapping(target = "paidAt",    ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    BillPayment toEntity(CreateBillPaymentRequest request);

    @Mapping(target = "providerName", source = "provider.name")
    @Mapping(target = "status", expression = "java(p.getStatus().name())")
    BillPaymentResponse toResponse(BillPayment p);
}
