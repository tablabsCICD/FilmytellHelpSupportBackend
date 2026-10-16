package com.filmytell.support.service;

import com.filmytell.support.dto.dashboard.DashboardSummaryResponse;
import com.filmytell.support.dto.dashboard.MemberWorkloadDto;
import com.filmytell.support.dto.dashboard.TeamWorkloadDto;
import com.filmytell.support.entity.Team;
import com.filmytell.support.entity.TeamMember;
import com.filmytell.support.entity.Ticket;
import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TaskStatus;
import com.filmytell.support.enums.TicketPriority;
import com.filmytell.support.enums.TicketStatus;
import com.filmytell.support.repository.TeamRepository;
import com.filmytell.support.repository.TicketRepository;
import com.filmytell.support.repository.TicketTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final TicketTaskRepository taskRepository;
    private final TeamRepository teamRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        long totalTickets = ticketRepository.count();
        long openTickets = ticketRepository.countOpenTickets();
        long slaBreachedTickets = ticketRepository.countSlaBreachedTickets();
        long aiResolvedTickets = ticketRepository.countAiResolvedTickets();
        long aiEscalatedTickets = ticketRepository.countAiEscalatedTickets();

        List<Ticket> resolvedList = ticketRepository.findByResolvedAtIsNotNull();
        Double avgResolutionTime = resolvedList.isEmpty() ? 0.0 : resolvedList.stream()
                .filter(t -> t.getCreatedAt() != null && t.getResolvedAt() != null)
                .mapToLong(t -> java.time.Duration.between(t.getCreatedAt(), t.getResolvedAt()).toMinutes())
                .average()
                .orElse(0.0);

        List<Ticket> respondedList = ticketRepository.findByFirstRespondedAtIsNotNull();
        Double avgFirstResponse = respondedList.isEmpty() ? 0.0 : respondedList.stream()
                .filter(t -> t.getCreatedAt() != null && t.getFirstRespondedAt() != null)
                .mapToLong(t -> java.time.Duration.between(t.getCreatedAt(), t.getFirstRespondedAt()).toMinutes())
                .average()
                .orElse(0.0);

        long totalDelegatedTasks = taskRepository.countTotalDelegatedTasks();
        Double avgDelegationDepth = taskRepository.findAverageDelegationDepth();

        Map<String, Long> statusCounts = new HashMap<>();
        for (Object[] row : ticketRepository.countTicketsByStatus()) {
            statusCounts.put(((TicketStatus) row[0]).name(), (Long) row[1]);
        }

        Map<String, Long> priorityCounts = new HashMap<>();
        for (Object[] row : ticketRepository.countTicketsByPriority()) {
            priorityCounts.put(((TicketPriority) row[0]).name(), (Long) row[1]);
        }

        Map<String, Long> sourceCounts = new HashMap<>();
        for (Object[] row : ticketRepository.countTicketsBySourcePortal()) {
            sourceCounts.put(((SourcePortal) row[0]).name(), (Long) row[1]);
        }

        return DashboardSummaryResponse.builder()
                .totalTickets(totalTickets)
                .openTickets(openTickets)
                .urgentTickets(priorityCounts.getOrDefault(TicketPriority.URGENT.name(), 0L))
                .highPriorityTickets(priorityCounts.getOrDefault(TicketPriority.HIGH.name(), 0L))
                .pendingUserTickets(statusCounts.getOrDefault(TicketStatus.PENDING_USER.name(), 0L))
                .inProgressTickets(statusCounts.getOrDefault(TicketStatus.IN_PROGRESS.name(), 0L))
                .resolvedTickets(statusCounts.getOrDefault(TicketStatus.RESOLVED.name(), 0L))
                .closedTickets(statusCounts.getOrDefault(TicketStatus.CLOSED.name(), 0L))
                .slaBreachedTickets(slaBreachedTickets)
                .aiResolvedTickets(aiResolvedTickets)
                .aiEscalatedTickets(aiEscalatedTickets)
                .averageResolutionTimeMinutes(avgResolutionTime != null ? Math.round(avgResolutionTime * 100.0) / 100.0 : 0.0)
                .averageFirstResponseTimeMinutes(avgFirstResponse != null ? Math.round(avgFirstResponse * 100.0) / 100.0 : 0.0)
                .totalDelegatedTasks(totalDelegatedTasks)
                .averageDelegationDepth(avgDelegationDepth != null ? Math.round(avgDelegationDepth * 100.0) / 100.0 : 0.0)
                .ticketsByStatus(statusCounts)
                .ticketsByPriority(priorityCounts)
                .ticketsBySourcePortal(sourceCounts)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TeamWorkloadDto> getTeamWorkloads() {
        List<Team> teams = teamRepository.findByActiveTrue();
        List<TeamWorkloadDto> workloads = new ArrayList<>();

        for (Team team : teams) {
            var teamTickets = ticketRepository.findByAssignedTeamId(team.getId());
            long total = teamTickets.size();
            long open = teamTickets.stream()
                    .filter(t -> t.getStatus() != TicketStatus.RESOLVED && t.getStatus() != TicketStatus.CLOSED)
                    .count();
            long urgent = teamTickets.stream()
                    .filter(t -> t.getPriority() == TicketPriority.URGENT && t.getStatus() != TicketStatus.CLOSED)
                    .count();

            List<MemberWorkloadDto> memberList = new ArrayList<>();
            if (team.getMembers() != null) {
                for (TeamMember tm : team.getMembers()) {
                    if (tm.isActive()) {
                        long activeTasks = taskRepository.findByAssignedToUserIdAndStatus(tm.getUser().getId(), TaskStatus.IN_PROGRESS).size()
                                + taskRepository.findByAssignedToUserIdAndStatus(tm.getUser().getId(), TaskStatus.PENDING).size();
                        long ownedTickets = ticketRepository.findByTicketOwnerId(tm.getUser().getId()).stream()
                                .filter(t -> t.getStatus() != TicketStatus.CLOSED)
                                .count();

                        memberList.add(MemberWorkloadDto.builder()
                                .userId(tm.getUser().getId())
                                .username(tm.getUser().getUsername())
                                .fullName(tm.getUser().getFullName())
                                .roleInTeam(tm.getRoleInTeam())
                                .activeTasksCount(activeTasks)
                                .ownedTicketsCount(ownedTickets)
                                .build());
                    }
                }
            }

            workloads.add(TeamWorkloadDto.builder()
                    .teamId(team.getId())
                    .teamName(team.getName())
                    .teamCode(team.getCode())
                    .totalAssignedTickets(total)
                    .openTickets(open)
                    .urgentTickets(urgent)
                    .memberWorkloads(memberList)
                    .build());
        }

        return workloads;
    }
}
