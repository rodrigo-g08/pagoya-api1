package com.hampcode.pagoya.billing.service;

import com.hampcode.pagoya.billing.dto.CreateRecurringBillPaymentRequest;
import com.hampcode.pagoya.billing.dto.RecurringBillPaymentResponse;
import com.hampcode.pagoya.billing.exception.InactiveProviderException;
import com.hampcode.pagoya.billing.exception.InvalidRecurringScheduleException;
import com.hampcode.pagoya.billing.exception.InvalidStatusTransitionException;
import com.hampcode.pagoya.billing.mapper.RecurringBillPaymentMapper;
import com.hampcode.pagoya.billing.model.BillPayment;
import com.hampcode.pagoya.billing.model.PaymentStatus;
import com.hampcode.pagoya.billing.model.RecurringBillPayment;
import com.hampcode.pagoya.billing.model.RecurringFrequency;
import com.hampcode.pagoya.billing.model.RecurringStatus;
import com.hampcode.pagoya.billing.model.ServiceProvider;
import com.hampcode.pagoya.billing.repository.BillPaymentRepository;
import com.hampcode.pagoya.billing.repository.RecurringBillPaymentRepository;
import com.hampcode.pagoya.billing.repository.ServiceProviderRepository;
import com.hampcode.pagoya.customer.model.Customer;
import com.hampcode.pagoya.customer.repository.CustomerRepository;
import com.hampcode.pagoya.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RecurringBillPaymentService implements IRecurringBillPaymentService {

    private final RecurringBillPaymentRepository recurringRepository;
    private final ServiceProviderRepository providerRepository;
    private final CustomerRepository customerRepository;
    private final BillPaymentRepository billPaymentRepository;
    private final RecurringBillPaymentMapper recurringMapper;

    @Override
    @Transactional
    public RecurringBillPaymentResponse schedule(CreateRecurringBillPaymentRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
            .orElseThrow(() -> new ResourceNotFoundException("cliente no encontrado"));

        ServiceProvider provider = providerRepository.findById(request.providerId())
            .orElseThrow(() -> new ResourceNotFoundException("proveedor no encontrado"));

        
        if (!provider.isActive()) {
            throw new InactiveProviderException();
        }

        
        if (request.frequency() == RecurringFrequency.MONTHLY && request.dayOfMonth() == null) {
            throw new InvalidRecurringScheduleException(
                "el dia del mes es obligatorio cuando la frecuencia es MONTHLY");
        }
        if (request.frequency() == RecurringFrequency.WEEKLY && request.dayOfWeek() == null) {
            throw new InvalidRecurringScheduleException(
                "el dia de la semana es obligatorio cuando la frecuencia es WEEKLY");
        }

        RecurringBillPayment entity = RecurringBillPayment.builder()
            .customer(customer)
            .provider(provider)
            .billCode(request.billCode())
            .amount(request.amount())
            .frequency(request.frequency())
            .dayOfMonth(request.dayOfMonth())
            .dayOfWeek(request.dayOfWeek())
            .status(RecurringStatus.ACTIVE)
            .nextRunAt(calculateNextRun(request.frequency(),
                                       request.dayOfMonth(),
                                       request.dayOfWeek()))
            .createdAt(LocalDateTime.now())
            .build();

        return recurringMapper.toResponse(recurringRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecurringBillPaymentResponse> findByCustomer(Long customerId, Pageable pageable) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("cliente no encontrado");
        }
        return recurringRepository.findByCustomer_Id(customerId, pageable)
            .map(recurringMapper::toResponse);
    }

    @Override
    @Transactional
    public RecurringBillPaymentResponse pause(Long id) {
        RecurringBillPayment entity = recurringRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("pago recurrente no encontrado"));

        // RN-R04: solo se puede pausar uno ACTIVE
        if (entity.getStatus() != RecurringStatus.ACTIVE) {
            throw new InvalidStatusTransitionException(
                "solo se puede pausar un pago recurrente activo");
        }
        entity.setStatus(RecurringStatus.PAUSED);
        return recurringMapper.toResponse(recurringRepository.save(entity));
    }

    @Override
    @Transactional
    public RecurringBillPaymentResponse resume(Long id) {
        RecurringBillPayment entity = recurringRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("pago recurrente no encontrado"));

        
        if (entity.getStatus() != RecurringStatus.PAUSED) {
            throw new InvalidStatusTransitionException(
                "solo se puede reanudar un pago recurrente pausado");
        }
        entity.setStatus(RecurringStatus.ACTIVE);
        
        entity.setNextRunAt(calculateNextRun(entity.getFrequency(),
                                             entity.getDayOfMonth(),
                                             entity.getDayOfWeek()));
        return recurringMapper.toResponse(recurringRepository.save(entity));
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        RecurringBillPayment entity = recurringRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("pago recurrente no encontrado"));

        if (entity.getStatus() == RecurringStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                "el pago recurrente ya esta cancelado");
        }
        entity.setStatus(RecurringStatus.CANCELLED);
        recurringRepository.save(entity);
    }

    @Override
    @Transactional
    public void executeDue(RecurringBillPayment recurring) {
        LocalDateTime now = LocalDateTime.now();

        BillPayment payment = BillPayment.builder()
            .customer(recurring.getCustomer())
            .provider(recurring.getProvider())
            .billCode(recurring.getBillCode())
            .amount(recurring.getAmount())
            .status(PaymentStatus.PAID)
            .paidAt(now)
            .createdAt(now)
            .build();
        billPaymentRepository.save(payment);

        recurring.setNextRunAt(calculateNextRun(
            recurring.getFrequency(),
            recurring.getDayOfMonth(),
            recurring.getDayOfWeek()));
        recurringRepository.save(recurring);
    }

    private LocalDateTime calculateNextRun(RecurringFrequency frequency,
                                           Integer dayOfMonth,
                                           Integer dayOfWeek) {
        LocalDate today = LocalDate.now();
        if (frequency == RecurringFrequency.MONTHLY) {
            LocalDate next = today.withDayOfMonth(dayOfMonth);
            if (!next.isAfter(today)) {
                next = next.plusMonths(1);
            }
            return next.atStartOfDay();
        }
        // WEEKLY
        DayOfWeek targetDow = DayOfWeek.of(dayOfWeek);
        LocalDate next = today;
        while (next.getDayOfWeek() != targetDow || !next.isAfter(today)) {
            next = next.plusDays(1);
        }
        return next.atStartOfDay();
    }
}
