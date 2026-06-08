package com.hampcode.pagoya.billing.mapper;

import com.hampcode.pagoya.billing.dto.ServiceProviderResponse;
import com.hampcode.pagoya.billing.model.ServiceProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServiceProviderMapper {

    @Mapping(target = "category", expression = "java(p.getCategory().name())")
    ServiceProviderResponse toResponse(ServiceProvider p);
}
