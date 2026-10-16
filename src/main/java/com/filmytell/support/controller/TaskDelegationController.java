package com.filmytell.support.controller;

import com.filmytell.support.dto.common.ApiResponse;
import com.filmytell.support.dto.task.CompleteTaskRequest;
import com.filmytell.support.dto.task.DelegateTaskRequest;
import com.filmytell.support.dto.task.OverrideTaskRequest;
import com.filmytell.support.dto.task.ReturnTaskRequest;
import com.filmytell.support.dto.task.TicketTaskResponse;
import com.filmytell.support.service.TaskDelegationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Task Delegation Hierarchy", description = "Endpoints for multi-level task delegations (A -> B -> C -> D), task completion, and returns")
public class TaskDelegationController {

    private final TaskDelegationService taskDelegationService;

    @PostMapping("/tickets/{ticketId}/delegate")
    @PreAuthorize("hasAnyAuthority('TICKET_DELEGATE', 'ROLE_ADMIN', 'ROLE_SUPPORT_MANAGER', 'ROLE_SUPPORT_MEMBER', 'ROLE_TECHNICAL_SUPPORT_MEMBER')")
    @Operation(summary = "Delegate ticket task to another agent (Maintains A -> B -> C chain and preserves ticket owner)")
    public ResponseEntity<ApiResponse<TicketTaskResponse>> delegateTask(
            @PathVariable Long ticketId,
            @Valid @RequestBody DelegateTaskRequest request
    ) {
        TicketTaskResponse response = taskDelegationService.delegateTask(ticketId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Task delegated successfully", response));
    }

    @GetMapping("/tickets/{ticketId}/tasks")
    @PreAuthorize("hasAnyAuthority('TICKET_VIEW', 'ROLE_ADMIN')")
    @Operation(summary = "Get complete hierarchical task delegation tree for ticket")
    public ResponseEntity<ApiResponse<List<TicketTaskResponse>>> getTasksForTicket(@PathVariable Long ticketId) {
        List<TicketTaskResponse> tasks = taskDelegationService.getTasksForTicket(ticketId);
        return ResponseEntity.ok(ApiResponse.ok(tasks));
    }

    @PostMapping("/tasks/{taskId}/start")
    @PreAuthorize("hasAnyAuthority('TICKET_UPDATE', 'ROLE_ADMIN', 'ROLE_SUPPORT_MEMBER', 'ROLE_TECHNICAL_SUPPORT_MEMBER')")
    @Operation(summary = "Start working on delegated task (Moves status to IN_PROGRESS)")
    public ResponseEntity<ApiResponse<TicketTaskResponse>> startTask(@PathVariable Long taskId) {
        TicketTaskResponse response = taskDelegationService.startTask(taskId);
        return ResponseEntity.ok(ApiResponse.ok("Task marked as IN_PROGRESS", response));
    }

    @PostMapping("/tasks/{taskId}/complete")
    @PreAuthorize("hasAnyAuthority('TICKET_RESOLVE', 'ROLE_ADMIN', 'ROLE_SUPPORT_MEMBER', 'ROLE_TECHNICAL_SUPPORT_MEMBER')")
    @Operation(summary = "Complete delegated subtask (Cascades control back to parent delegator)")
    public ResponseEntity<ApiResponse<TicketTaskResponse>> completeTask(
            @PathVariable Long taskId,
            @Valid @RequestBody CompleteTaskRequest request
    ) {
        TicketTaskResponse response = taskDelegationService.completeTask(taskId, request);
        return ResponseEntity.ok(ApiResponse.ok("Task completed and returned to parent delegator", response));
    }

    @PostMapping("/tasks/{taskId}/return")
    @PreAuthorize("hasAnyAuthority('TICKET_DELEGATE', 'ROLE_ADMIN', 'ROLE_SUPPORT_MEMBER', 'ROLE_TECHNICAL_SUPPORT_MEMBER')")
    @Operation(summary = "Return task back to the delegator")
    public ResponseEntity<ApiResponse<TicketTaskResponse>> returnTask(
            @PathVariable Long taskId,
            @Valid @RequestBody ReturnTaskRequest request
    ) {
        TicketTaskResponse response = taskDelegationService.returnTask(taskId, request);
        return ResponseEntity.ok(ApiResponse.ok("Task returned to delegator", response));
    }

    @PostMapping("/tasks/{taskId}/override")
    @PreAuthorize("hasAnyAuthority('TICKET_TAKE_BACK', 'ROLE_ADMIN', 'ROLE_SUPPORT_MANAGER')")
    @Operation(summary = "Admin override or take back task delegation")
    public ResponseEntity<ApiResponse<TicketTaskResponse>> overrideTask(
            @PathVariable Long taskId,
            @Valid @RequestBody OverrideTaskRequest request
    ) {
        TicketTaskResponse response = taskDelegationService.overrideDelegation(taskId, request);
        return ResponseEntity.ok(ApiResponse.ok("Delegation overridden successfully", response));
    }
}
