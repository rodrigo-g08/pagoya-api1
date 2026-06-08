package com.hampcode.pagoya.billing.repository;

import com.hampcode.pagoya.billing.model.ServiceProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
    Page<ServiceProvider> findByActiveTrue(Pageable pageable);
}
