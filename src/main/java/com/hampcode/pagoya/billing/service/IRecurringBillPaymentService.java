package com.hampcode.pagoya.billing.service;

import com.hampcode.pagoya.billing.dto.CreateRecurringBillPaymentRequest;
import com.hampcode.pagoya.billing.dto.RecurringBillPaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IRecurringBillPaymentService {
    RecurringBillPaymentResponse schedule(CreateRecurringBillPaymentRequest request);
    Page<RecurringBillPaymentResponse> findByCustomer(Long customerId, Pageable pageable);
    RecurringBillPaymentResponse pause(Long id);
    RecurringBillPaymentResponse resume(Long id);
    void cancel(Long id);
    void executeDue(com.hampcode.pagoya.billing.model.RecurringBillPayment recurring);
}
