package com.filmytell.support.service;

import com.filmytell.support.dto.ticket.*;
import com.filmytell.support.entity.*;
import com.filmytell.support.enums.*;
import com.filmytell.support.exception.InvalidStatusTransitionException;
import com.filmytell.support.exception.ResourceNotFoundException;
import com.filmytell.support.exception.UnauthorizedOperationException;
import com.filmytell.support.mapper.TicketMapper;
import com.filmytell.support.repository.*;
import com.filmytell.support.security.SecurityUtils;
import com.filmytell.support.service.integration.AdminPortalTicketClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketWorkflowService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TicketTaskRepository taskRepository;
    private final TicketAssignmentRepository assignmentRepository;
    private final TicketPriorityHistoryRepository priorityHistoryRepository;
    private final TicketMapper ticketMapper;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final AdminPortalTicketClient adminPortalTicketClient;

    @Transactional
    public TicketResponse assignTicket(Long ticketId, AssignTicketRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        User targetUser = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssigneeId()));

        if (!targetUser.isActive()) {
            throw new IllegalArgumentException("Cannot assign ticket to inactive user: " + targetUser.getUsername());
        }

        Team targetTeam = request.getTeamId() != null
                ? teamRepository.findById(request.getTeamId()).orElse(null)
                : ticket.getAssignedTeam();

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null
                ? userRepository.findById(currentUserId).orElse(null)
                : targetUser;

        User previousAssignee = ticket.getCurrentAssignee();
        Team previousTeam = ticket.getAssignedTeam();

        // If ticket had no owner, targetUser becomes ticketOwner and currentAssignee
        if (ticket.getTicketOwner() == null) {
            ticket.setTicketOwner(targetUser);
        }
        ticket.setCurrentAssignee(targetUser);
        ticket.setDelegatedFrom(null);
        ticket.setAssignedTeam(targetTeam);

        if (ticket.getStatus() == TicketStatus.NEW || ticket.getStatus() == TicketStatus.AI_ANALYSIS) {
            ticket.setStatus(TicketStatus.ASSIGNED);
        }

        Ticket savedTicket = ticketRepository.save(ticket);

        // Record root task if not already present
        if (taskRepository.findByTicketIdAndParentTaskIsNull(ticketId).isEmpty()) {
            TicketTask rootTask = TicketTask.builder()
                    .ticket(savedTicket)
                    .parentTask(null)
                    .assignedFromUser(currentUser != null ? currentUser : targetUser)
                    .assignedToUser(targetUser)
                    .delegationLevel(0)
                    .reason(DelegationReason.OTHER)
                    .instructions("Primary ticket task assigned to " + targetUser.getFullName())
                    .status(TaskStatus.IN_PROGRESS)
                    .startedAt(Instant.now())
                    .build();
            taskRepository.save(rootTask);
        }

        // Record assignment log
        TicketAssignment assignment = TicketAssignment.builder()
                .ticket(savedTicket)
                .fromUser(previousAssignee)
                .toUser(targetUser)
                .fromTeam(previousTeam)
                .toTeam(targetTeam)
                .assignedByUser(currentUser != null ? currentUser : targetUser)
                .reason(request.getReason())
                .build();
        assignmentRepository.save(assignment);

        // Record Audit event
        auditService.recordEvent(
                savedTicket,
                HistoryEventType.ASSIGNED,
                currentUser,
                previousAssignee != null ? previousAssignee.getFullName() : "UNASSIGNED",
                targetUser.getFullName(),
                "Assigned to " + targetUser.getFullName() + (request.getReason() != null ? " (" + request.getReason() + ")" : ""),
                "{\"assigneeId\":" + targetUser.getId() + ",\"teamId\":" + (targetTeam != null ? targetTeam.getId() : "null") + "}"
        );

        // Send notification
        notificationService.sendNotification(
                targetUser,
                savedTicket,
                NotificationType.TICKET_ASSIGNED,
                "Ticket Assigned: #" + savedTicket.getTicketNumber(),
                "You have been assigned Ticket #" + savedTicket.getTicketNumber() + ": " + savedTicket.getSubject()
        );

        return ticketMapper.toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse transferOwnership(Long ticketId, TransferOwnershipRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        User newOwner = userRepository.findById(request.getNewOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getNewOwnerId()));

        if (!newOwner.isActive()) {
            throw new IllegalArgumentException("Cannot transfer ownership to inactive user: " + newOwner.getUsername());
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        User previousOwner = ticket.getTicketOwner();
        ticket.setTicketOwner(newOwner);
        Ticket savedTicket = ticketRepository.save(ticket);

        auditService.recordEvent(
                savedTicket,
                HistoryEventType.OWNER_CHANGED,
                currentUser,
                previousOwner != null ? previousOwner.getFullName() : "NONE",
                newOwner.getFullName(),
                "Ticket ownership transferred to " + newOwner.getFullName() + ". Reason: " + request.getReason(),
                "{\"newOwnerId\":" + newOwner.getId() + ",\"reason\":\"" + request.getReason() + "\"}"
        );

        return ticketMapper.toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse updateStatus(Long ticketId, UpdateStatusRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        TicketStatus current = ticket.getStatus();
        TicketStatus target = request.getStatus();

        if (!current.canTransitionTo(target) && !SecurityUtils.hasAuthority("ROLE_ADMIN")) {
            throw new InvalidStatusTransitionException(current, target);
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        ticket.setStatus(target);
        if (target == TicketStatus.RESOLVED && ticket.getResolvedAt() == null) {
            ticket.setResolvedAt(Instant.now());
        } else if (target == TicketStatus.CLOSED && ticket.getClosedAt() == null) {
            ticket.setClosedAt(Instant.now());
        }

        Ticket saved = ticketRepository.save(ticket);

        if ("ADMIN_PORTAL".equalsIgnoreCase(saved.getSourceSystem()) && saved.getSourceTicketId() != null) {
            boolean isResolved = (target == TicketStatus.RESOLVED || target == TicketStatus.CLOSED);
            adminPortalTicketClient.updateTicketOnAdminPortal(
                    saved.getSourceTicketId(),
                    request.getReason() != null ? request.getReason() : ("Status updated to " + target),
                    isResolved
            );
        }

        auditService.recordEvent(
                saved,
                HistoryEventType.STATUS_CHANGED,
                currentUser,
                current.name(),
                target.name(),
                "Status updated to " + target + (request.getReason() != null ? ": " + request.getReason() : ""),
                "{\"from\":\"" + current + "\",\"to\":\"" + target + "\"}"
        );

        return ticketMapper.toResponse(saved);
    }

    @Transactional
    public TicketResponse updatePriority(Long ticketId, UpdatePriorityRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        TicketPriority oldPriority = ticket.getPriority();
        TicketPriority newPriority = request.getPriority();

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        ticket.setPriority(newPriority);
        Ticket saved = ticketRepository.save(ticket);

        // Record priority history
        TicketPriorityHistory history = TicketPriorityHistory.builder()
                .ticket(saved)
                .oldPriority(oldPriority)
                .newPriority(newPriority)
                .changedByUser(currentUser != null ? currentUser : saved.getTicketOwner())
                .changeReason(request.getReason())
                .build();
        priorityHistoryRepository.save(history);

        auditService.recordEvent(
                saved,
                HistoryEventType.PRIORITY_CHANGED,
                currentUser,
                oldPriority.name(),
                newPriority.name(),
                "Priority changed from " + oldPriority + " to " + newPriority + ". Reason: " + request.getReason(),
                "{\"old\":\"" + oldPriority + "\",\"new\":\"" + newPriority + "\",\"reason\":\"" + request.getReason() + "\"}"
        );

        return ticketMapper.toResponse(saved);
    }

    @Transactional
    public TicketResponse resolveTicket(Long ticketId, String resolutionNotes) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new InvalidStatusTransitionException(ticket.getStatus(), TicketStatus.RESOLVED);
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolvedAt(Instant.now());
        Ticket saved = ticketRepository.save(ticket);

        if ("ADMIN_PORTAL".equalsIgnoreCase(saved.getSourceSystem()) && saved.getSourceTicketId() != null) {
            adminPortalTicketClient.updateTicketOnAdminPortal(
                    saved.getSourceTicketId(),
                    resolutionNotes != null ? resolutionNotes : "Ticket resolved by Help Support Team",
                    true
            );
        }

        auditService.recordEvent(
                saved,
                HistoryEventType.RESOLVED,
                currentUser,
                ticket.getStatus().name(),
                TicketStatus.RESOLVED.name(),
                "Ticket marked as RESOLVED" + (resolutionNotes != null ? ": " + resolutionNotes : ""),
                "{\"resolutionNotes\":\"" + resolutionNotes + "\"}"
        );

        User notifyTarget = saved.getTicketOwner();
        if (notifyTarget != null) {
            notificationService.sendNotification(
                    notifyTarget,
                    saved,
                    NotificationType.TICKET_RESOLVED,
                    "Ticket Resolved: #" + saved.getTicketNumber(),
                    "Ticket #" + saved.getTicketNumber() + " has been marked as resolved."
            );
        }

        return ticketMapper.toResponse(saved);
    }

    @Transactional
    public TicketResponse closeTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(Instant.now());
        Ticket saved = ticketRepository.save(ticket);

        auditService.recordEvent(
                saved,
                HistoryEventType.CLOSED,
                currentUser,
                ticket.getStatus().name(),
                TicketStatus.CLOSED.name(),
                "Ticket permanently closed",
                "{}"
        );

        return ticketMapper.toResponse(saved);
    }

    @Transactional
    public TicketResponse reopenTicket(Long ticketId, String reason) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setResolvedAt(null);
        Ticket saved = ticketRepository.save(ticket);

        auditService.recordEvent(
                saved,
                HistoryEventType.REOPENED,
                currentUser,
                TicketStatus.RESOLVED.name(),
                TicketStatus.IN_PROGRESS.name(),
                "Ticket reopened. Reason: " + reason,
                "{\"reason\":\"" + reason + "\"}"
        );

        User notifyTarget = saved.getCurrentAssignee() != null ? saved.getCurrentAssignee() : saved.getTicketOwner();
        if (notifyTarget != null) {
            notificationService.sendNotification(
                    notifyTarget,
                    saved,
                    NotificationType.TICKET_REOPENED,
                    "Ticket Reopened: #" + saved.getTicketNumber(),
                    "Ticket #" + saved.getTicketNumber() + " has been reopened: " + reason
            );
        }

        return ticketMapper.toResponse(saved);
    }
}
