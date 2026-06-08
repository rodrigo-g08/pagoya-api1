package com.hampcode.pagoya.transfer.mapper;

import com.hampcode.pagoya.transfer.dto.TransferResponse;
import com.hampcode.pagoya.transfer.model.Transfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransferMapper {
    @Mapping(target = "sourceAccountNumber", source = "sourceAccount.accountNumber")
    @Mapping(target = "targetAccountNumber", source = "targetAccount.accountNumber")
    TransferResponse toResponse(Transfer transfer);
}
