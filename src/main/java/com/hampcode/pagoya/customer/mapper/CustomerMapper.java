package com.hampcode.pagoya.customer.mapper;

import com.hampcode.pagoya.customer.dto.CustomerResponse;
import com.hampcode.pagoya.customer.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    @Mapping(target = "userId", source = "user.id")
    CustomerResponse toResponse(Customer customer);
}
