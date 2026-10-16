package com.filmytell.support.service;

import com.filmytell.support.dto.sla.SlaPolicyRequest;
import com.filmytell.support.dto.sla.SlaPolicyResponse;
import com.filmytell.support.entity.SlaPolicy;
import com.filmytell.support.entity.Ticket;
import com.filmytell.support.entity.User;
import com.filmytell.support.enums.HistoryEventType;
import com.filmytell.support.enums.NotificationType;
import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TicketPriority;
import com.filmytell.support.exception.ResourceNotFoundException;
import com.filmytell.support.repository.SlaPolicyRepository;
import com.filmytell.support.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlaService {

    private final SlaPolicyRepository slaPolicyRepository;
    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public SlaPolicy findMatchingPolicy(TicketPriority priority, SourcePortal sourcePortal) {
        if (sourcePortal != null) {
            var policyOpt = slaPolicyRepository.findByPriorityAndSourcePortalAndActiveTrue(priority, sourcePortal);
            if (policyOpt.isPresent()) {
                return policyOpt.get();
            }
        }
        return slaPolicyRepository.findByPriorityAndSourcePortalIsNullAndActiveTrue(priority)
                .orElse(null);
    }

    public void calculateAndApplySla(Ticket ticket) {
        SlaPolicy policy = findMatchingPolicy(ticket.getPriority(), ticket.getSourcePortal());
        if (policy != null) {
            ticket.setSlaPolicy(policy);
            Instant now = ticket.getCreatedAt() != null ? ticket.getCreatedAt() : Instant.now();
            ticket.setSlaResponseDueAt(now.plus(policy.getFirstResponseTimeMinutes(), ChronoUnit.MINUTES));
            ticket.setSlaResolutionDueAt(now.plus(policy.getResolutionTimeMinutes(), ChronoUnit.MINUTES));
        } else {
            // Default fallback SLA: 60m response, 24h resolution
            Instant now = ticket.getCreatedAt() != null ? ticket.getCreatedAt() : Instant.now();
            ticket.setSlaResponseDueAt(now.plus(60, ChronoUnit.MINUTES));
            ticket.setSlaResolutionDueAt(now.plus(1440, ChronoUnit.MINUTES));
        }
    }

    @Transactional
    public void evaluateSlaBreaches() {
        Instant now = Instant.now();
        List<Ticket> overdueTickets = ticketRepository.findPendingSlaBreaches(now);

        for (Ticket ticket : overdueTickets) {
            ticket.setSlaBreached(true);
            ticketRepository.save(ticket);

            auditService.recordEvent(
                    ticket,
                    HistoryEventType.STATUS_CHANGED,
                    null,
                    "SLA_ACTIVE",
                    "SLA_BREACHED",
                    "SLA Resolution Target Breached (Due at: " + ticket.getSlaResolutionDueAt() + ")",
                    "{\"dueAt\":\"" + ticket.getSlaResolutionDueAt() + "\"}"
            );

            User notifyTarget = ticket.getCurrentAssignee() != null ? ticket.getCurrentAssignee() : ticket.getTicketOwner();
            if (notifyTarget != null) {
                notificationService.sendNotification(
                        notifyTarget,
                        ticket,
                        NotificationType.SLA_BREACHED,
                        "SLA Breached: Ticket #" + ticket.getTicketNumber(),
                        "Ticket #" + ticket.getTicketNumber() + " has exceeded its resolution SLA target."
                );
            }
            log.warn("SLA Breached for Ticket #{}", ticket.getTicketNumber());
        }
    }

    @Transactional
    public SlaPolicyResponse createPolicy(SlaPolicyRequest request) {
        SlaPolicy policy = SlaPolicy.builder()
                .name(request.getName())
                .priority(request.getPriority())
                .sourcePortal(request.getSourcePortal())
                .firstResponseTimeMinutes(request.getFirstResponseTimeMinutes())
                .resolutionTimeMinutes(request.getResolutionTimeMinutes())
                .active(true)
                .build();

        return toResponse(slaPolicyRepository.save(policy));
    }

    @Transactional(readOnly = true)
    public List<SlaPolicyResponse> getAllPolicies() {
        return slaPolicyRepository.findAll().stream().map(this::toResponse).toList();
    }

    private SlaPolicyResponse toResponse(SlaPolicy p) {
        return SlaPolicyResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .priority(p.getPriority())
                .sourcePortal(p.getSourcePortal())
                .firstResponseTimeMinutes(p.getFirstResponseTimeMinutes())
                .resolutionTimeMinutes(p.getResolutionTimeMinutes())
                .active(p.isActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
