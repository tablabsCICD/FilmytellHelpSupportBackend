package com.filmytell.support.scheduler;

import com.filmytell.support.service.SlaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlaMonitoringScheduler {

    private final SlaService slaService;

    @Scheduled(cron = "${app.sla.check-interval-cron:0 */5 * * * *}")
    public void monitorSlaBreaches() {
        log.debug("Executing scheduled SLA breach monitor...");
        try {
            slaService.evaluateSlaBreaches();
        } catch (Exception ex) {
            log.error("Error executing SLA monitoring scheduler: {}", ex.getMessage());
        }
    }
}
