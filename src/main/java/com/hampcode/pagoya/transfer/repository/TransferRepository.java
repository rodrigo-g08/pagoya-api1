package com.hampcode.pagoya.transfer.repository;

import com.hampcode.pagoya.transfer.model.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    @EntityGraph(attributePaths = {"sourceAccount", "targetAccount"})
    Page<Transfer> findBySourceAccount_Id(Long accountId, Pageable pageable);

    @Query(value = "SELECT * FROM fn_transfer_report_by_currency()",
           nativeQuery = true)
    List<Object[]> reportByCurrencyRaw();

    @Query(value = "SELECT * FROM fn_transfer_report_by_day(:from, :to)",
           nativeQuery = true)
    List<Object[]> reportByDayRaw(@Param("from") LocalDate from,
                                  @Param("to") LocalDate to);

    @Query(value = "SELECT * FROM fn_transfer_report_by_status()",
           nativeQuery = true)
    List<Object[]> reportByStatusRaw();
}
