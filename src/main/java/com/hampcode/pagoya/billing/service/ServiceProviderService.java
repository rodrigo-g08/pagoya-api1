package com.hampcode.pagoya.billing.service;

import com.hampcode.pagoya.billing.dto.ServiceProviderResponse;
import com.hampcode.pagoya.billing.mapper.ServiceProviderMapper;
import com.hampcode.pagoya.billing.repository.ServiceProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceProviderService implements IServiceProviderService {

    private final ServiceProviderRepository providerRepository;
    private final ServiceProviderMapper providerMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceProviderResponse> findAllActive(Pageable pageable) {
        return providerRepository.findByActiveTrue(pageable)
            .map(providerMapper::toResponse);
    }
}
