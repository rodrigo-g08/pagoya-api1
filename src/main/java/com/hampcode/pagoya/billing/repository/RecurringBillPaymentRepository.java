package com.hampcode.pagoya.billing.repository;

import com.hampcode.pagoya.billing.model.RecurringBillPayment;
import com.hampcode.pagoya.billing.model.RecurringStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RecurringBillPaymentRepository extends JpaRepository<RecurringBillPayment, Long> {
    Page<RecurringBillPayment> findByCustomer_Id(Long customerId, Pageable pageable);

    List<RecurringBillPayment> findByStatusAndNextRunAtLessThanEqual(
        RecurringStatus status, LocalDateTime nextRunAt);
}
