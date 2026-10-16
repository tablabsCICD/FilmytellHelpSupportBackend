package com.filmytell.support.service;

import com.filmytell.support.dto.common.PageResponse;
import com.filmytell.support.dto.ticket.*;
import com.filmytell.support.entity.*;
import com.filmytell.support.enums.*;
import com.filmytell.support.exception.DuplicateResourceException;
import com.filmytell.support.exception.ResourceNotFoundException;
import com.filmytell.support.mapper.TicketMapper;
import com.filmytell.support.repository.*;
import com.filmytell.support.repository.specification.TicketSpecification;
import com.filmytell.support.security.SecurityUtils;
import com.filmytell.support.service.ai.AiSupportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TicketAttachmentRepository attachmentRepository;
    private final TicketMapper ticketMapper;
    private final AuditService auditService;
    private final SlaService slaService;
    private final AiSupportService aiSupportService;

    private static final AtomicLong TICKET_COUNTER = new AtomicLong(System.currentTimeMillis() % 1000000);

    @Transactional
    public TicketResponse createInternalTicket(CreateTicketRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        Team team = null;
        if (request.getAssignedTeamId() != null) {
            team = teamRepository.findById(request.getAssignedTeamId()).orElse(null);
        }

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId()).orElse(null);
        }

        String ticketNumber = generateTicketNumber(request.getSourcePortal());

        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketNumber)
                .sourcePortal(request.getSourcePortal())
                .externalTicketId(request.getExternalTicketId())
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .category(request.getCategory())
                .subCategory(request.getSubCategory())
                .subject(request.getSubject())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : TicketPriority.MEDIUM)
                .status(assignee != null ? TicketStatus.ASSIGNED : TicketStatus.NEW)
                .ticketOwner(assignee != null ? assignee : currentUser)
                .currentAssignee(assignee)
                .assignedTeam(team)
                .aiEligible(true)
                .build();

        slaService.calculateAndApplySla(ticket);
        Ticket savedTicket = ticketRepository.save(ticket);

        // Attachments metadata
        if (request.getAttachmentUrls() != null) {
            for (String url : request.getAttachmentUrls()) {
                TicketAttachment attachment = TicketAttachment.builder()
                        .ticket(savedTicket)
                        .fileName("attachment_" + System.currentTimeMillis())
                        .fileUrl(url)
                        .uploadedByUser(currentUser)
                        .internal(false)
                        .build();
                attachmentRepository.save(attachment);
            }
        }

        auditService.recordEvent(
                savedTicket,
                HistoryEventType.TICKET_CREATED,
                currentUser,
                null,
                TicketStatus.NEW.name(),
                "Ticket #" + ticketNumber + " created from " + request.getSourcePortal(),
                "{\"source\":\"" + request.getSourcePortal() + "\"}"
        );

        // Trigger AI analysis asynchronously if not manually assigned
        if (assignee == null) {
            aiSupportService.processTicketAsync(savedTicket.getId());
        }

        return ticketMapper.toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse createExternalTicket(ExternalCreateTicketRequest request) {
        // Idempotency check: (source_portal + external_ticket_id) must be unique
        if (request.getExternalTicketId() != null &&
                ticketRepository.existsBySourcePortalAndExternalTicketId(request.getSourcePortal(), request.getExternalTicketId())) {
            log.info("Idempotent hit: Ticket with external ID {} already exists for portal {}",
                    request.getExternalTicketId(), request.getSourcePortal());
            Ticket existing = ticketRepository.findBySourcePortalAndExternalTicketId(request.getSourcePortal(), request.getExternalTicketId())
                    .orElseThrow();
            return ticketMapper.toResponse(existing);
        }

        String ticketNumber = generateTicketNumber(request.getSourcePortal());

        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketNumber)
                .sourcePortal(request.getSourcePortal())
                .externalTicketId(request.getExternalTicketId())
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .category(request.getCategory())
                .subCategory(request.getSubCategory())
                .subject(request.getSubject())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : TicketPriority.MEDIUM)
                .status(TicketStatus.NEW)
                .aiEligible(true)
                .build();

        slaService.calculateAndApplySla(ticket);
        Ticket savedTicket = ticketRepository.save(ticket);

        if (request.getAttachmentUrls() != null) {
            for (String url : request.getAttachmentUrls()) {
                TicketAttachment attachment = TicketAttachment.builder()
                        .ticket(savedTicket)
                        .fileName("portal_attachment_" + System.currentTimeMillis())
                        .fileUrl(url)
                        .internal(false)
                        .build();
                attachmentRepository.save(attachment);
            }
        }

        auditService.recordEvent(
                savedTicket,
                HistoryEventType.TICKET_CREATED,
                null,
                null,
                TicketStatus.NEW.name(),
                "External ticket received from " + request.getSourcePortal() + " (Ext ID: " + request.getExternalTicketId() + ")",
                "{\"externalId\":\"" + request.getExternalTicketId() + "\"}"
        );

        // Process with AI Grounding
        aiSupportService.processTicketAsync(savedTicket.getId());

        return ticketMapper.toResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public TicketDetailResponse getTicketDetails(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
        return ticketMapper.toDetailResponse(ticket);
    }

    @Transactional(readOnly = true)
    public TicketDetailResponse getTicketByNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "ticketNumber", ticketNumber));
        return ticketMapper.toDetailResponse(ticket);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> searchTickets(
            String ticketNumber,
            SourcePortal sourcePortal,
            String category,
            String subCategory,
            TicketStatus status,
            TicketPriority priority,
            Long teamId,
            Long ownerId,
            Long assigneeId,
            Boolean slaBreached,
            Boolean aiResolved,
            String search,
            Instant createdAfter,
            Instant createdBefore,
            Pageable pageable
    ) {
        Specification<Ticket> spec = TicketSpecification.filterTickets(
                ticketNumber, sourcePortal, category, subCategory, status,
                priority, teamId, ownerId, assigneeId, slaBreached,
                aiResolved, search, createdAfter, createdBefore
        );

        Page<Ticket> page = ticketRepository.findAll(spec, pageable);
        return PageResponse.<TicketResponse>builder()
                .success(true)
                .data(page.getContent().stream().map(ticketMapper::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private String generateTicketNumber(SourcePortal portal) {
        String prefix = switch (portal) {
            case FILMYTELL -> "FLM";
            case FILMYTELL_PARTNER -> "FLM-PTR";
            case PRODUCTION_HOUSE -> "PRD";
            case ADMIN_PORTAL -> "ADM";
        };
        long seq = TICKET_COUNTER.incrementAndGet();
        return String.format("%s-%d", prefix, seq);
    }
}
