package com.filmytell.support.scheduler;

import com.filmytell.support.dto.ticket.TicketSyncSummaryDto;
import com.filmytell.support.service.TicketSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "admin.portal.sync.enabled", havingValue = "true", matchIfMissing = false)
public class TicketSyncScheduler {

    private final TicketSyncService ticketSyncService;

    @Scheduled(cron = "${admin.portal.sync.cron:0 */15 * * * *}")
    public void scheduledSync() {
        log.info("[Ticket Sync Scheduler] Executing scheduled ticket synchronization from Admin Portal...");
        try {
            TicketSyncSummaryDto summary = ticketSyncService.syncTicketsFromAdminPortal();
            log.info("[Ticket Sync Scheduler] Scheduled sync finished: fetched={}, imported={}, skipped={}",
                    summary.getTotalFetched(), summary.getNewTickets(), summary.getDuplicatesSkipped());
        } catch (Exception e) {
            log.error("[Ticket Sync Scheduler] Error during scheduled sync: {}", e.getMessage(), e);
        }
    }
}
