package com.filmytell.support.controller;

import com.filmytell.support.dto.common.ApiResponse;
import com.filmytell.support.dto.kanban.KanbanBoardResponse;
import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TicketPriority;
import com.filmytell.support.service.KanbanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/kanban")
@RequiredArgsConstructor
@Tag(name = "Kanban Board", description = "Endpoints for Kanban board view grouped by ticket lifecycle statuses")
public class KanbanController {

    private final KanbanService kanbanService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TICKET_VIEW', 'ROLE_ADMIN')")
    @Operation(summary = "Get Kanban board columns with ticket cards and optional team/assignee filters")
    public ResponseEntity<ApiResponse<KanbanBoardResponse>> getKanbanBoard(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) SourcePortal sourcePortal,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        KanbanBoardResponse board = kanbanService.getKanbanBoard(teamId, assigneeId, priority, sourcePortal, category, search);
        return ResponseEntity.ok(ApiResponse.ok(board));
    }
}
