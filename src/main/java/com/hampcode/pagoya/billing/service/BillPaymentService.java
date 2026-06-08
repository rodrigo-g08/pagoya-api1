package com.hampcode.pagoya.billing.service;

import com.hampcode.pagoya.billing.dto.BillPaymentResponse;
import com.hampcode.pagoya.billing.dto.CreateBillPaymentRequest;
import com.hampcode.pagoya.billing.dto.PaymentByCategoryResponse;
import com.hampcode.pagoya.billing.exception.DuplicateBillPaymentException;
import com.hampcode.pagoya.billing.exception.InactiveProviderException;
import com.hampcode.pagoya.billing.mapper.BillPaymentMapper;
import com.hampcode.pagoya.billing.model.BillPayment;
import com.hampcode.pagoya.billing.model.PaymentStatus;
import com.hampcode.pagoya.billing.model.ServiceProvider;
import com.hampcode.pagoya.billing.repository.BillPaymentRepository;
import com.hampcode.pagoya.billing.repository.ServiceProviderRepository;
import com.hampcode.pagoya.customer.model.Customer;
import com.hampcode.pagoya.customer.repository.CustomerRepository;
import com.hampcode.pagoya.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillPaymentService implements IBillPaymentService {

    private final BillPaymentRepository billPaymentRepository;
    private final ServiceProviderRepository providerRepository;
    private final CustomerRepository customerRepository;
    private final BillPaymentMapper billPaymentMapper;

    @Override
    @Transactional
    public BillPaymentResponse pay(CreateBillPaymentRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
            .orElseThrow(() -> new ResourceNotFoundException("cliente no encontrado"));

        ServiceProvider provider = providerRepository.findById(request.providerId())
            .orElseThrow(() -> new ResourceNotFoundException("proveedor no encontrado"));

        
        if (!provider.isActive()) {
            throw new InactiveProviderException();
        }

        
        if (billPaymentRepository.existsByCustomer_IdAndProvider_IdAndBillCode(
                customer.getId(), provider.getId(), request.billCode())) {
            throw new DuplicateBillPaymentException();
        }

        BillPayment entity = billPaymentMapper.toEntity(request);
        entity.setCustomer(customer);
        entity.setProvider(provider);
        entity.setStatus(PaymentStatus.PAID);
        entity.setPaidAt(LocalDateTime.now());
        entity.setCreatedAt(LocalDateTime.now());

        return billPaymentMapper.toResponse(billPaymentRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillPaymentResponse> findByCustomer(Long customerId, Pageable pageable) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("cliente no encontrado");
        }
        return billPaymentRepository.findByCustomer_Id(customerId, pageable)
            .map(billPaymentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentByCategoryResponse> reportByCategory(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("cliente no encontrado");
        }
        return billPaymentRepository.getPaymentsByCategory(customerId).stream()
            .map(r -> new PaymentByCategoryResponse(
                (String) r[0],
                ((Number) r[1]).longValue(),
                (BigDecimal) r[2]))
            .toList();
    }
}
