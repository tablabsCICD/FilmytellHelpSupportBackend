package com.filmytell.support.dto.ticket;

import com.filmytell.support.dto.conversation.TicketAttachmentResponse;
import com.filmytell.support.dto.conversation.TicketMessageResponse;
import com.filmytell.support.dto.conversation.TicketNoteResponse;
import com.filmytell.support.dto.task.TicketTaskResponse;
import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TicketPriority;
import com.filmytell.support.enums.TicketStatus;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDetailResponse {

    private Long id;
    private String ticketNumber;
    private SourcePortal sourcePortal;
    private String externalTicketId;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String category;
    private String subCategory;
    private String subject;
    private String description;
    private TicketPriority priority;
    private TicketStatus status;

    private Long ticketOwnerId;
    private String ticketOwnerName;
    private String ticketOwnerEmail;

    private Long currentAssigneeId;
    private String currentAssigneeName;
    private String currentAssigneeEmail;

    private Long delegatedFromId;
    private String delegatedFromName;

    private Long assignedTeamId;
    private String assignedTeamName;

    private Instant slaResponseDueAt;
    private Instant slaResolutionDueAt;
    private Instant firstRespondedAt;
    private boolean slaBreached;

    private boolean aiEligible;
    private Double aiConfidence;
    private boolean aiResolved;

    private List<TicketTaskResponse> tasks;
    private List<TicketMessageResponse> messages;
    private List<TicketNoteResponse> notes;
    private List<TicketAttachmentResponse> attachments;
    private List<TicketHistoryDto> history;
    private List<TicketAssignmentDto> assignments;
    private List<TicketPriorityHistoryDto> priorityHistory;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant resolvedAt;
    private Instant closedAt;
}
