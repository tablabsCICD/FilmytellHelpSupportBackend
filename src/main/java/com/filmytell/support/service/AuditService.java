package com.filmytell.support.service;

import com.filmytell.support.entity.Ticket;
import com.filmytell.support.entity.TicketHistory;
import com.filmytell.support.entity.User;
import com.filmytell.support.enums.HistoryEventType;
import com.filmytell.support.repository.TicketHistoryRepository;
import com.filmytell.support.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final TicketHistoryRepository ticketHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public void recordEvent(
            Ticket ticket,
            HistoryEventType eventType,
            User performedByUser,
            String oldValue,
            String newValue,
            String description,
            String metadata
    ) {
        String actorName = performedByUser != null ? performedByUser.getFullName() : SecurityUtils.getCurrentUserFullName();

        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .eventType(eventType)
                .performedByUser(performedByUser)
                .performedByName(actorName)
                .oldValue(oldValue)
                .newValue(newValue)
                .description(description)
                .metadata(metadata)
                .build();

        ticketHistoryRepository.save(history);
        log.info("Ticket #{} Audit Event: [{}] by {} - {}", ticket.getTicketNumber(), eventType, actorName, description);
    }
}
