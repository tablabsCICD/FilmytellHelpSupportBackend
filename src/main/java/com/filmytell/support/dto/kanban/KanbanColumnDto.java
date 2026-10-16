package com.filmytell.support.dto.kanban;

import com.filmytell.support.dto.ticket.TicketResponse;
import com.filmytell.support.enums.TicketStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanColumnDto {

    private TicketStatus status;
    private String title;
    private long count;
    private List<TicketResponse> tickets;
}
