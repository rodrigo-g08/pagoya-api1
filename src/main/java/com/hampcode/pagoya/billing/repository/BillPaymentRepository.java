package com.hampcode.pagoya.billing.repository;

import com.hampcode.pagoya.billing.model.BillPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillPaymentRepository extends JpaRepository<BillPayment, Long> {

    boolean existsByCustomer_IdAndProvider_IdAndBillCode(
        Long customerId, Long providerId, String billCode);

    Page<BillPayment> findByCustomer_Id(Long customerId, Pageable pageable);

    @Query(value = "SELECT * FROM sp_payments_by_category(:customerId)", nativeQuery = true)
    List<Object[]> getPaymentsByCategory(@Param("customerId") Long customerId);
}
