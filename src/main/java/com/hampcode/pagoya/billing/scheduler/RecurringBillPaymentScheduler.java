package com.hampcode.pagoya.billing.scheduler;

import com.hampcode.pagoya.billing.model.RecurringStatus;
import com.hampcode.pagoya.billing.repository.RecurringBillPaymentRepository;
import com.hampcode.pagoya.billing.service.IRecurringBillPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RecurringBillPaymentScheduler {

    private final RecurringBillPaymentRepository recurringRepository;
    private final IRecurringBillPaymentService recurringService;


     // Cada hora busca pagos recurrentes ACTIVE cuyo nextRunAt ya paso
     // y los ejecuta uno por uno (crea BillPayment + avanza nextRunAt).

     // Para probar en demo: cambiar a "0 * * * * *" (cada minuto).
     // @Scheduled(cron = "0 0 3 * * *")   3 AM diario
    // @Scheduled(cron = "0 0 * * * *")   cada hora
     //@Scheduled(cron = "0 */15 * * * *")   cada 15 min
    // @Scheduled(cron = "0 0 8 * * MON-FRI")   8 AM de lunes a viernes

    @Scheduled(cron = "0 * * * * *")
    public void runDuePayments() {
        recurringRepository
            .findByStatusAndNextRunAtLessThanEqual(
                RecurringStatus.ACTIVE, LocalDateTime.now())
            .forEach(recurringService::executeDue);
    }
}
