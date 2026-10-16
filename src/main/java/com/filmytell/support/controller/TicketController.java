package com.filmytell.support.controller;

import com.filmytell.support.dto.common.ApiResponse;
import com.filmytell.support.dto.common.PageResponse;
import com.filmytell.support.dto.ticket.*;
import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TicketPriority;
import com.filmytell.support.enums.TicketStatus;
import com.filmytell.support.service.TicketService;
import com.filmytell.support.service.TicketWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Tag(name = "Ticket Management", description = "Endpoints for creating, searching, updating, and transitioning tickets")
public class TicketController {

    private final TicketService ticketService;
    private final TicketWorkflowService workflowService;
    private final com.filmytell.support.service.TicketSyncService ticketSyncService;

    @PostMapping("/sync")
    @PreAuthorize("hasAnyAuthority('TICKET_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Synchronize tickets from Admin Portal API")
    public ResponseEntity<ApiResponse<com.filmytell.support.dto.ticket.TicketSyncSummaryDto>> syncTickets() {
        com.filmytell.support.dto.ticket.TicketSyncSummaryDto summary = ticketSyncService.syncTicketsFromAdminPortal();
        return ResponseEntity.ok(ApiResponse.ok("Ticket synchronization completed", summary));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('TICKET_CREATE', 'ROLE_ADMIN')")
    @Operation(summary = "Create an internal support ticket")
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        TicketResponse response = ticketService.createInternalTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Ticket created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TICKET_VIEW', 'ROLE_ADMIN')")
    @Operation(summary = "Search and filter tickets with pagination and sorting")
    public ResponseEntity<PageResponse<TicketResponse>> searchTickets(
            @RequestParam(required = false) String ticketNumber,
            @RequestParam(required = false) SourcePortal sourcePortal,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subCategory,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Boolean slaBreached,
            @RequestParam(required = false) Boolean aiResolved,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PageResponse<TicketResponse> response = ticketService.searchTickets(
                ticketNumber, sourcePortal, category, subCategory, status,
                priority, teamId, ownerId, assigneeId, slaBreached,
                aiResolved, search, createdAfter, createdBefore, pageable
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TICKET_VIEW', 'ROLE_ADMIN')")
    @Operation(summary = "Get comprehensive ticket details (Tasks, Messages, Notes, SLA, History)")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> getTicketDetails(@PathVariable Long id) {
        TicketDetailResponse response = ticketService.getTicketDetails(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyAuthority('TICKET_ASSIGN', 'ROLE_ADMIN', 'ROLE_SUPPORT_MANAGER')")
    @Operation(summary = "Assign ticket to user & team")
    public ResponseEntity<ApiResponse<TicketResponse>> assignTicket(
            @PathVariable Long id,
            @Valid @RequestBody AssignTicketRequest request
    ) {
        TicketResponse response = workflowService.assignTicket(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Ticket assigned successfully", response));
    }

    @PostMapping("/{id}/transfer-ownership")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPPORT_MANAGER')")
    @Operation(summary = "Transfer overall ticket ownership to another agent")
    public ResponseEntity<ApiResponse<TicketResponse>> transferOwnership(
            @PathVariable Long id,
            @Valid @RequestBody TransferOwnershipRequest request
    ) {
        TicketResponse response = workflowService.transferOwnership(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Ownership transferred successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('STATUS_UPDATE', 'TICKET_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Update ticket status with workflow validation")
    public ResponseEntity<ApiResponse<TicketResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        TicketResponse response = workflowService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Status updated successfully", response));
    }

    @PatchMapping("/{id}/priority")
    @PreAuthorize("hasAnyAuthority('PRIORITY_UPDATE', 'ROLE_ADMIN', 'ROLE_SUPPORT_MANAGER')")
    @Operation(summary = "Update ticket priority with audit reason")
    public ResponseEntity<ApiResponse<TicketResponse>> updatePriority(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePriorityRequest request
    ) {
        TicketResponse response = workflowService.updatePriority(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Priority updated successfully", response));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyAuthority('TICKET_RESOLVE', 'ROLE_ADMIN', 'ROLE_SUPPORT_MANAGER', 'ROLE_SUPPORT_MEMBER')")
    @Operation(summary = "Mark ticket as RESOLVED")
    public ResponseEntity<ApiResponse<TicketResponse>> resolveTicket(
            @PathVariable Long id,
            @RequestParam(required = false) String notes
    ) {
        TicketResponse response = workflowService.resolveTicket(id, notes);
        return ResponseEntity.ok(ApiResponse.ok("Ticket resolved successfully", response));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyAuthority('TICKET_CLOSE', 'ROLE_ADMIN', 'ROLE_SUPPORT_MANAGER')")
    @Operation(summary = "Permanently close ticket")
    public ResponseEntity<ApiResponse<TicketResponse>> closeTicket(@PathVariable Long id) {
        TicketResponse response = workflowService.closeTicket(id);
        return ResponseEntity.ok(ApiResponse.ok("Ticket closed successfully", response));
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasAnyAuthority('TICKET_REOPEN', 'ROLE_ADMIN', 'ROLE_SUPPORT_MANAGER')")
    @Operation(summary = "Reopen a resolved ticket")
    public ResponseEntity<ApiResponse<TicketResponse>> reopenTicket(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Reopened for further review") String reason
    ) {
        TicketResponse response = workflowService.reopenTicket(id, reason);
        return ResponseEntity.ok(ApiResponse.ok("Ticket reopened successfully", response));
    }
}
