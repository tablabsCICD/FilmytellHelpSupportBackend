package com.filmytell.support.mapper;

import com.filmytell.support.dto.ticket.*;
import com.filmytell.support.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TicketMapper {

    private final TaskMapper taskMapper;
    private final ConversationMapper conversationMapper;

    public TicketResponse toResponse(Ticket ticket) {
        if (ticket == null) return null;

        int activeLevel = 0;
        if (ticket.getTasks() != null && !ticket.getTasks().isEmpty()) {
            activeLevel = ticket.getTasks().stream()
                    .filter(t -> t.getStatus() == com.filmytell.support.enums.TaskStatus.IN_PROGRESS || t.getStatus() == com.filmytell.support.enums.TaskStatus.PENDING)
                    .mapToInt(TicketTask::getDelegationLevel)
                    .max()
                    .orElse(0);
        }

        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .sourcePortal(ticket.getSourcePortal())
                .externalTicketId(ticket.getExternalTicketId())
                .customerId(ticket.getCustomerId())
                .customerName(ticket.getCustomerName())
                .customerEmail(ticket.getCustomerEmail())
                .category(ticket.getCategory())
                .subCategory(ticket.getSubCategory())
                .subject(ticket.getSubject())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .ticketOwnerId(ticket.getTicketOwner() != null ? ticket.getTicketOwner().getId() : null)
                .ticketOwnerName(ticket.getTicketOwner() != null ? ticket.getTicketOwner().getFullName() : null)
                .currentAssigneeId(ticket.getCurrentAssignee() != null ? ticket.getCurrentAssignee().getId() : null)
                .currentAssigneeName(ticket.getCurrentAssignee() != null ? ticket.getCurrentAssignee().getFullName() : null)
                .delegatedFromId(ticket.getDelegatedFrom() != null ? ticket.getDelegatedFrom().getId() : null)
                .delegatedFromName(ticket.getDelegatedFrom() != null ? ticket.getDelegatedFrom().getFullName() : null)
                .assignedTeamId(ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getId() : null)
                .assignedTeamName(ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getName() : null)
                .slaResponseDueAt(ticket.getSlaResponseDueAt())
                .slaResolutionDueAt(ticket.getSlaResolutionDueAt())
                .slaBreached(ticket.isSlaBreached())
                .aiEligible(ticket.isAiEligible())
                .aiConfidence(ticket.getAiConfidence())
                .aiResolved(ticket.isAiResolved())
                .activeDelegationLevel(activeLevel)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .closedAt(ticket.getClosedAt())
                .build();
    }

    public TicketDetailResponse toDetailResponse(Ticket ticket) {
        if (ticket == null) return null;

        List<com.filmytell.support.dto.task.TicketTaskResponse> taskDtos = ticket.getTasks() != null
                ? ticket.getTasks().stream()
                .filter(t -> t.getParentTask() == null) // Root tasks with recursive children
                .map(taskMapper::toResponse)
                .toList()
                : Collections.emptyList();

        List<com.filmytell.support.dto.conversation.TicketMessageResponse> messageDtos = ticket.getMessages() != null
                ? ticket.getMessages().stream().map(conversationMapper::toMessageResponse).toList()
                : Collections.emptyList();

        List<com.filmytell.support.dto.conversation.TicketNoteResponse> noteDtos = ticket.getNotes() != null
                ? ticket.getNotes().stream().map(conversationMapper::toNoteResponse).toList()
                : Collections.emptyList();

        List<com.filmytell.support.dto.conversation.TicketAttachmentResponse> attachmentDtos = ticket.getAttachments() != null
                ? ticket.getAttachments().stream().map(conversationMapper::toAttachmentResponse).toList()
                : Collections.emptyList();

        List<TicketHistoryDto> historyDtos = ticket.getHistories() != null
                ? ticket.getHistories().stream().map(this::toHistoryDto).toList()
                : Collections.emptyList();

        List<TicketAssignmentDto> assignmentDtos = ticket.getAssignments() != null
                ? ticket.getAssignments().stream().map(this::toAssignmentDto).toList()
                : Collections.emptyList();

        List<TicketPriorityHistoryDto> priorityDtos = ticket.getPriorityHistories() != null
                ? ticket.getPriorityHistories().stream().map(this::toPriorityHistoryDto).toList()
                : Collections.emptyList();

        return TicketDetailResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .sourcePortal(ticket.getSourcePortal())
                .externalTicketId(ticket.getExternalTicketId())
                .customerId(ticket.getCustomerId())
                .customerName(ticket.getCustomerName())
                .customerEmail(ticket.getCustomerEmail())
                .category(ticket.getCategory())
                .subCategory(ticket.getSubCategory())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .ticketOwnerId(ticket.getTicketOwner() != null ? ticket.getTicketOwner().getId() : null)
                .ticketOwnerName(ticket.getTicketOwner() != null ? ticket.getTicketOwner().getFullName() : null)
                .ticketOwnerEmail(ticket.getTicketOwner() != null ? ticket.getTicketOwner().getEmail() : null)
                .currentAssigneeId(ticket.getCurrentAssignee() != null ? ticket.getCurrentAssignee().getId() : null)
                .currentAssigneeName(ticket.getCurrentAssignee() != null ? ticket.getCurrentAssignee().getFullName() : null)
                .currentAssigneeEmail(ticket.getCurrentAssignee() != null ? ticket.getCurrentAssignee().getEmail() : null)
                .delegatedFromId(ticket.getDelegatedFrom() != null ? ticket.getDelegatedFrom().getId() : null)
                .delegatedFromName(ticket.getDelegatedFrom() != null ? ticket.getDelegatedFrom().getFullName() : null)
                .assignedTeamId(ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getId() : null)
                .assignedTeamName(ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getName() : null)
                .slaResponseDueAt(ticket.getSlaResponseDueAt())
                .slaResolutionDueAt(ticket.getSlaResolutionDueAt())
                .firstRespondedAt(ticket.getFirstRespondedAt())
                .slaBreached(ticket.isSlaBreached())
                .aiEligible(ticket.isAiEligible())
                .aiConfidence(ticket.getAiConfidence())
                .aiResolved(ticket.isAiResolved())
                .tasks(taskDtos)
                .messages(messageDtos)
                .notes(noteDtos)
                .attachments(attachmentDtos)
                .history(historyDtos)
                .assignments(assignmentDtos)
                .priorityHistory(priorityDtos)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .closedAt(ticket.getClosedAt())
                .build();
    }

    public TicketHistoryDto toHistoryDto(TicketHistory history) {
        if (history == null) return null;
        return TicketHistoryDto.builder()
                .id(history.getId())
                .eventType(history.getEventType())
                .performedById(history.getPerformedByUser() != null ? history.getPerformedByUser().getId() : null)
                .performedByName(history.getPerformedByName() != null ? history.getPerformedByName()
                        : (history.getPerformedByUser() != null ? history.getPerformedByUser().getFullName() : "SYSTEM"))
                .oldValue(history.getOldValue())
                .newValue(history.getNewValue())
                .description(history.getDescription())
                .metadata(history.getMetadata())
                .createdAt(history.getCreatedAt())
                .build();
    }

    public TicketAssignmentDto toAssignmentDto(TicketAssignment assignment) {
        if (assignment == null) return null;
        return TicketAssignmentDto.builder()
                .id(assignment.getId())
                .fromUserId(assignment.getFromUser() != null ? assignment.getFromUser().getId() : null)
                .fromUserName(assignment.getFromUser() != null ? assignment.getFromUser().getFullName() : null)
                .toUserId(assignment.getToUser() != null ? assignment.getToUser().getId() : null)
                .toUserName(assignment.getToUser() != null ? assignment.getToUser().getFullName() : null)
                .fromTeamId(assignment.getFromTeam() != null ? assignment.getFromTeam().getId() : null)
                .fromTeamName(assignment.getFromTeam() != null ? assignment.getFromTeam().getName() : null)
                .toTeamId(assignment.getToTeam() != null ? assignment.getToTeam().getId() : null)
                .toTeamName(assignment.getToTeam() != null ? assignment.getToTeam().getName() : null)
                .assignedById(assignment.getAssignedByUser().getId())
                .assignedByName(assignment.getAssignedByUser().getFullName())
                .reason(assignment.getReason())
                .createdAt(assignment.getCreatedAt())
                .build();
    }

    public TicketPriorityHistoryDto toPriorityHistoryDto(TicketPriorityHistory history) {
        if (history == null) return null;
        return TicketPriorityHistoryDto.builder()
                .id(history.getId())
                .oldPriority(history.getOldPriority())
                .newPriority(history.getNewPriority())
                .changedById(history.getChangedByUser().getId())
                .changedByName(history.getChangedByUser().getFullName())
                .changeReason(history.getChangeReason())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
