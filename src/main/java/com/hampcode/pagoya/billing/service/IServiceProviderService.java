package com.hampcode.pagoya.billing.service;

import com.hampcode.pagoya.billing.dto.ServiceProviderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IServiceProviderService {
    Page<ServiceProviderResponse> findAllActive(Pageable pageable);
}
