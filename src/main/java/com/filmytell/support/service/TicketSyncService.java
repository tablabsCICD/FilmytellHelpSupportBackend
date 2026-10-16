package com.filmytell.support.service;

import com.filmytell.support.dto.adminportal.AdminPortalTicketDto;
import com.filmytell.support.dto.adminportal.AdminPortalUserDto;
import com.filmytell.support.dto.ticket.TicketSyncSummaryDto;
import com.filmytell.support.entity.Ticket;
import com.filmytell.support.enums.HistoryEventType;
import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TicketPriority;
import com.filmytell.support.enums.TicketStatus;
import com.filmytell.support.repository.TicketRepository;
import com.filmytell.support.service.ai.AiSupportService;
import com.filmytell.support.service.integration.AdminPortalTicketClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketSyncService {

    private final AdminPortalTicketClient adminPortalTicketClient;
    private final TicketRepository ticketRepository;
    private final SlaService slaService;
    private final AuditService auditService;
    private final AiSupportService aiSupportService;

    private static final String SOURCE_SYSTEM_ADMIN_PORTAL = "ADMIN_PORTAL";
    private static final AtomicLong SYNC_TICKET_COUNTER = new AtomicLong(System.currentTimeMillis() % 1000000);

    @Transactional
    public TicketSyncSummaryDto syncTicketsFromAdminPortal() {
        log.info("[Ticket Sync] Starting synchronization");

        List<AdminPortalTicketDto> fetchedTickets = adminPortalTicketClient.fetchAllTickets();
        int totalFetched = fetchedTickets.size();
        int newTickets = 0;
        int existingTickets = 0;
        int duplicatesSkipped = 0;

        for (AdminPortalTicketDto dto : fetchedTickets) {
            String sourceTicketId = dto.getSourceTicketId();
            if (sourceTicketId == null || sourceTicketId.trim().isEmpty()) {
                continue;
            }

            // Duplicate Check
            boolean exists = ticketRepository.existsBySourceSystemAndSourceTicketId(SOURCE_SYSTEM_ADMIN_PORTAL, sourceTicketId)
                    || ticketRepository.existsBySourcePortalAndExternalTicketId(SourcePortal.ADMIN_PORTAL, sourceTicketId);

            if (exists) {
                existingTickets++;
                duplicatesSkipped++;
                log.info("[Ticket Sync] Skipped existing ticket sourceTicketId={}", sourceTicketId);
                continue;
            }

            try {
                String ticketNumber = "ADM-" + SYNC_TICKET_COUNTER.incrementAndGet();

                // Extract customer details
                String customerName = "Admin Portal User";
                String customerEmail = dto.getEmail();
                AdminPortalUserDto user = dto.getUser();
                if (user != null) {
                    if (user.getFirstName() != null || user.getLastName() != null) {
                        customerName = ((user.getFirstName() != null ? user.getFirstName() : "") + " " +
                                (user.getLastName() != null ? user.getLastName() : "")).trim();
                    }
                    if ((customerEmail == null || customerEmail.isEmpty()) && user.getEmail() != null) {
                        customerEmail = user.getEmail();
                    }
                }
                if (customerName.isEmpty() && dto.getMobileNumber() != null) {
                    customerName = dto.getMobileNumber();
                }

                String topic = dto.getTopic() != null && !dto.getTopic().isBlank()
                        ? dto.getTopic()
                        : "Admin Portal Ticket #" + sourceTicketId;

                String description = dto.getDescription() != null && !dto.getDescription().isBlank()
                        ? dto.getDescription()
                        : topic;

                String category = dto.getCategory() != null && !dto.getCategory().isBlank()
                        ? dto.getCategory()
                        : "General Support";

                TicketStatus status = (Boolean.TRUE.equals(dto.getIsResolved()))
                        ? TicketStatus.RESOLVED
                        : TicketStatus.NEW;

                Ticket ticket = Ticket.builder()
                        .ticketNumber(ticketNumber)
                        .sourcePortal(SourcePortal.ADMIN_PORTAL)
                        .externalTicketId(sourceTicketId)
                        .sourceSystem(SOURCE_SYSTEM_ADMIN_PORTAL)
                        .sourceTicketId(sourceTicketId)
                        .customerName(customerName)
                        .customerEmail(customerEmail)
                        .customerId(dto.getMobileNumber())
                        .category(category)
                        .subject(topic)
                        .description(description)
                        .priority(TicketPriority.MEDIUM)
                        .status(status)
                        .aiEligible(true)
                        .build();

                slaService.calculateAndApplySla(ticket);
                Ticket savedTicket = ticketRepository.saveAndFlush(ticket);

                auditService.recordEvent(
                        savedTicket,
                        HistoryEventType.TICKET_CREATED,
                        null,
                        null,
                        TicketStatus.NEW.name(),
                        "Synchronized from Admin Portal (sourceTicketId: " + sourceTicketId + ")",
                        "{\"sourceSystem\":\"ADMIN_PORTAL\",\"sourceTicketId\":\"" + sourceTicketId + "\"}"
                );

                aiSupportService.processTicketAsync(savedTicket.getId());

                newTickets++;
                log.info("[Ticket Sync] Imported ticket sourceTicketId={}", sourceTicketId);
            } catch (DataIntegrityViolationException e) {
                existingTickets++;
                duplicatesSkipped++;
                log.info("[Ticket Sync] Concurrent duplicate caught for sourceTicketId={}", sourceTicketId);
            } catch (Exception e) {
                log.error("[Ticket Sync] Failed to import ticket sourceTicketId={}: {}", sourceTicketId, e.getMessage());
            }
        }

        log.info("[Ticket Sync] Completed. fetched={}, imported={}, skipped={}", totalFetched, newTickets, duplicatesSkipped);

        return TicketSyncSummaryDto.builder()
                .success(true)
                .totalFetched(totalFetched)
                .newTickets(newTickets)
                .existingTickets(existingTickets)
                .duplicatesSkipped(duplicatesSkipped)
                .build();
    }
}
