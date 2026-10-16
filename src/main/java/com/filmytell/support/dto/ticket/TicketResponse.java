package com.filmytell.support.dto.ticket;

import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TicketPriority;
import com.filmytell.support.enums.TicketStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {

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
    private TicketPriority priority;
    private TicketStatus status;

    private Long ticketOwnerId;
    private String ticketOwnerName;

    private Long currentAssigneeId;
    private String currentAssigneeName;

    private Long delegatedFromId;
    private String delegatedFromName;

    private Long assignedTeamId;
    private String assignedTeamName;

    private Instant slaResponseDueAt;
    private Instant slaResolutionDueAt;
    private boolean slaBreached;

    private boolean aiEligible;
    private Double aiConfidence;
    private boolean aiResolved;

    private int activeDelegationLevel;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant resolvedAt;
    private Instant closedAt;
}
