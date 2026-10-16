package com.filmytell.support.dto.kanban;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanBoardResponse {

    private List<KanbanColumnDto> columns;
    private long totalTickets;
}
