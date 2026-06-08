package com.hampcode.pagoya.account.mapper;

import com.hampcode.pagoya.account.dto.AccountBalanceResponse;
import com.hampcode.pagoya.account.dto.AccountResponse;
import com.hampcode.pagoya.account.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(target = "customerId", source = "customer.id")
    AccountResponse toResponse(Account account);
    AccountBalanceResponse toBalance(Account account);
}
