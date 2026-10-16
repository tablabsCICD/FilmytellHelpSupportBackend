package com.filmytell.support.dto.dashboard;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {

    private long totalTickets;
    private long openTickets;
    private long urgentTickets;
    private long highPriorityTickets;
    private long pendingUserTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private long closedTickets;

    private long slaBreachedTickets;
    private long aiResolvedTickets;
    private long aiEscalatedTickets;

    private double averageResolutionTimeMinutes;
    private double averageFirstResponseTimeMinutes;

    private long totalDelegatedTasks;
    private double averageDelegationDepth;

    private Map<String, Long> ticketsByStatus;
    private Map<String, Long> ticketsByPriority;
    private Map<String, Long> ticketsBySourcePortal;
}
