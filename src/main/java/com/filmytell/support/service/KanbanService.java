package com.filmytell.support.service;

import com.filmytell.support.dto.kanban.KanbanBoardResponse;
import com.filmytell.support.dto.kanban.KanbanColumnDto;
import com.filmytell.support.dto.ticket.TicketResponse;
import com.filmytell.support.entity.Ticket;
import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TicketPriority;
import com.filmytell.support.enums.TicketStatus;
import com.filmytell.support.mapper.TicketMapper;
import com.filmytell.support.repository.TicketRepository;
import com.filmytell.support.repository.specification.TicketSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KanbanService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    @Transactional(readOnly = true)
    public KanbanBoardResponse getKanbanBoard(
            Long teamId,
            Long assigneeId,
            TicketPriority priority,
            SourcePortal sourcePortal,
            String category,
            String search
    ) {
        Specification<Ticket> spec = TicketSpecification.filterTickets(
                null, sourcePortal, category, null, null,
                priority, teamId, null, assigneeId, null, null, search, null, null
        );

        List<Ticket> tickets = ticketRepository.findAll(spec);
        List<TicketResponse> responses = tickets.stream().map(ticketMapper::toResponse).toList();

        Map<TicketStatus, List<TicketResponse>> groupedByStatus = responses.stream()
                .collect(Collectors.groupingBy(TicketResponse::getStatus));

        List<KanbanColumnDto> columns = new ArrayList<>();
        for (TicketStatus status : TicketStatus.values()) {
            List<TicketResponse> colTickets = groupedByStatus.getOrDefault(status, List.of());
            columns.add(KanbanColumnDto.builder()
                    .status(status)
                    .title(formatColumnTitle(status))
                    .count(colTickets.size())
                    .tickets(colTickets)
                    .build());
        }

        return KanbanBoardResponse.builder()
                .columns(columns)
                .totalTickets(responses.size())
                .build();
    }

    private String formatColumnTitle(TicketStatus status) {
        return switch (status) {
            case NEW -> "New";
            case AI_ANALYSIS -> "AI Analyzing";
            case AI_RESOLVED -> "AI Resolved";
            case ASSIGNED -> "Assigned";
            case IN_PROGRESS -> "In Progress";
            case PENDING_USER -> "Pending Customer";
            case RESOLVED -> "Resolved";
            case CLOSED -> "Closed";
        };
    }
}
