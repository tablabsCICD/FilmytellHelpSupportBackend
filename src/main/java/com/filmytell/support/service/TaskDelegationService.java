package com.filmytell.support.service;

import com.filmytell.support.dto.task.CompleteTaskRequest;
import com.filmytell.support.dto.task.DelegateTaskRequest;
import com.filmytell.support.dto.task.OverrideTaskRequest;
import com.filmytell.support.dto.task.ReturnTaskRequest;
import com.filmytell.support.dto.task.TicketTaskResponse;
import com.filmytell.support.entity.Ticket;
import com.filmytell.support.entity.TicketTask;
import com.filmytell.support.entity.User;
import com.filmytell.support.exception.ErrorCode;
import com.filmytell.support.enums.HistoryEventType;
import com.filmytell.support.enums.NotificationType;
import com.filmytell.support.enums.TaskStatus;
import com.filmytell.support.enums.TicketStatus;
import com.filmytell.support.exception.DelegationException;
import com.filmytell.support.exception.ResourceNotFoundException;
import com.filmytell.support.exception.UnauthorizedOperationException;
import com.filmytell.support.mapper.TaskMapper;
import com.filmytell.support.repository.TicketRepository;
import com.filmytell.support.repository.TicketTaskRepository;
import com.filmytell.support.repository.UserRepository;
import com.filmytell.support.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskDelegationService {

    private final TicketRepository ticketRepository;
    private final TicketTaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Value("${app.delegation.max-depth:5}")
    private int maxDelegationDepth;

    @Transactional
    public TicketTaskResponse delegateTask(Long ticketId, DelegateTaskRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        if (ticket.getStatus() == TicketStatus.CLOSED || ticket.getStatus() == TicketStatus.RESOLVED) {
            throw new DelegationException("Cannot delegate tasks on a " + ticket.getStatus() + " ticket");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null
                ? userRepository.findById(currentUserId).orElse(null)
                : null;

        // Current assignee must perform the delegation unless Admin
        if (currentUser != null && ticket.getCurrentAssignee() != null
                && !ticket.getCurrentAssignee().getId().equals(currentUser.getId())
                && !SecurityUtils.hasAuthority("ROLE_ADMIN")
                && !SecurityUtils.hasAuthority("ROLE_SUPPORT_MANAGER")) {
            throw new UnauthorizedOperationException("Only the current task assignee or an Admin can delegate this ticket");
        }

        User delegator = ticket.getCurrentAssignee() != null ? ticket.getCurrentAssignee() : currentUser;
        if (delegator == null) {
            throw new DelegationException("Ticket has no current assignee to delegate from");
        }

        User targetUser = userRepository.findById(request.getAssignToUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssignToUserId()));

        // Rule 1: Target user must be active
        if (!targetUser.isActive()) {
            throw new DelegationException("Cannot delegate task to an inactive user: " + targetUser.getUsername(), ErrorCode.USER_NOT_ACTIVE);
        }

        // Rule 2: Cannot delegate to oneself (A -> A is forbidden)
        if (delegator.getId().equals(targetUser.getId())) {
            throw new DelegationException("Self-delegation is not allowed. A user cannot delegate a task to themselves.", ErrorCode.DELEGATION_SELF_NOT_ALLOWED);
        }

        // Retrieve active tasks for parent hierarchy
        List<TicketTask> activeTasks = taskRepository.findActiveTasksByTicketId(ticketId);
        TicketTask parentTask = activeTasks.isEmpty() ? null : activeTasks.get(0);

        int currentLevel = parentTask != null ? parentTask.getDelegationLevel() : 0;
        int nextLevel = currentLevel + 1;

        // Rule 3: Enforce max delegation depth
        if (nextLevel > maxDelegationDepth) {
            throw new DelegationException("Maximum delegation depth exceeded (Limit: " + maxDelegationDepth + ")", ErrorCode.DELEGATION_MAX_DEPTH_EXCEEDED);
        }

        // Rule 4: Prevent circular delegation (A -> B -> A is forbidden)
        if (isCircularDelegation(parentTask, targetUser.getId(), ticket.getTicketOwner())) {
            throw new DelegationException("Circular delegation detected: User " + targetUser.getFullName() + " is already in the active delegation chain.", ErrorCode.DELEGATION_CIRCULAR_LOOP);
        }

        // Create new delegated task
        TicketTask newTask = TicketTask.builder()
                .ticket(ticket)
                .parentTask(parentTask)
                .assignedFromUser(delegator)
                .assignedToUser(targetUser)
                .delegationLevel(nextLevel)
                .reason(request.getReason())
                .instructions(request.getInstructions())
                .status(TaskStatus.PENDING)
                .dueAt(request.getDueAt())
                .build();

        TicketTask savedTask = taskRepository.save(newTask);

        // Update ticket's current assignee (Owner remains UNCHANGED!)
        ticket.setCurrentAssignee(targetUser);
        ticket.setDelegatedFrom(delegator);
        if (ticket.getStatus() == TicketStatus.ASSIGNED || ticket.getStatus() == TicketStatus.NEW) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }
        ticketRepository.save(ticket);

        // Record Audit event
        auditService.recordEvent(
                ticket,
                HistoryEventType.TASK_DELEGATED,
                currentUser,
                delegator.getFullName() + " (Level " + currentLevel + ")",
                targetUser.getFullName() + " (Level " + nextLevel + ")",
                String.format("Task delegated from %s to %s. Reason: %s", delegator.getFullName(), targetUser.getFullName(), request.getReason()),
                String.format("{\"level\":%d,\"reason\":\"%s\",\"instructions\":\"%s\"}", nextLevel, request.getReason(), request.getInstructions())
        );

        // Send notification to delegatee
        notificationService.sendNotification(
                targetUser,
                ticket,
                NotificationType.TASK_DELEGATED,
                "New Task Delegated: Ticket #" + ticket.getTicketNumber(),
                String.format("%s delegated a task on Ticket #%s to you: %s", delegator.getFullName(), ticket.getTicketNumber(), request.getInstructions())
        );

        log.info("Ticket #{} task delegated [Level {} -> {}]: {} -> {}",
                ticket.getTicketNumber(), currentLevel, nextLevel, delegator.getUsername(), targetUser.getUsername());

        return taskMapper.toResponse(savedTask);
    }

    @Transactional
    public TicketTaskResponse startTask(Long taskId) {
        TicketTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId != null && !task.getAssignedToUser().getId().equals(currentUserId)
                && !SecurityUtils.hasAuthority("ROLE_ADMIN")) {
            throw new UnauthorizedOperationException("Only the assigned user can start this task");
        }

        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setStartedAt(Instant.now());
        TicketTask saved = taskRepository.save(task);

        Ticket ticket = task.getTicket();
        if (ticket.getStatus() != TicketStatus.IN_PROGRESS) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            ticketRepository.save(ticket);
        }

        auditService.recordEvent(
                ticket,
                HistoryEventType.TASK_STARTED,
                task.getAssignedToUser(),
                "PENDING",
                "IN_PROGRESS",
                "Task started by " + task.getAssignedToUser().getFullName() + " (Level " + task.getDelegationLevel() + ")",
                "{\"taskId\":" + taskId + "}"
        );

        return taskMapper.toResponse(saved);
    }

    @Transactional
    public TicketTaskResponse completeTask(Long taskId, CompleteTaskRequest request) {
        TicketTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        if (currentUser != null && !task.getAssignedToUser().getId().equals(currentUser.getId())
                && !SecurityUtils.hasAuthority("ROLE_ADMIN")
                && !SecurityUtils.hasAuthority("ROLE_SUPPORT_MANAGER")) {
            throw new UnauthorizedOperationException("Only the assigned user or an Admin can complete this task");
        }

        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(Instant.now());
        task.setCompletedByUser(currentUser != null ? currentUser : task.getAssignedToUser());
        task.setCompletionNotes(request.getCompletionNotes());
        TicketTask saved = taskRepository.save(task);

        Ticket ticket = task.getTicket();

        // Control cascades back to the delegator (parent task or ticket owner)
        if (task.getParentTask() != null) {
            ticket.setCurrentAssignee(task.getParentTask().getAssignedToUser());
            ticket.setDelegatedFrom(task.getParentTask().getAssignedFromUser());
        } else {
            // Root task completed -> return to Ticket Owner
            ticket.setCurrentAssignee(ticket.getTicketOwner());
            ticket.setDelegatedFrom(null);
        }
        ticketRepository.save(ticket);

        auditService.recordEvent(
                ticket,
                HistoryEventType.TASK_COMPLETED,
                currentUser,
                "IN_PROGRESS",
                "COMPLETED",
                String.format("Task completed by %s (Level %d): %s",
                        task.getAssignedToUser().getFullName(), task.getDelegationLevel(), request.getCompletionNotes()),
                "{\"taskId\":" + taskId + ",\"notes\":\"" + request.getCompletionNotes() + "\"}"
        );

        // Notify delegator that task is completed
        notificationService.sendNotification(
                task.getAssignedFromUser(),
                ticket,
                NotificationType.TASK_COMPLETED,
                "Task Completed: Ticket #" + ticket.getTicketNumber(),
                String.format("%s completed the delegated task: %s", task.getAssignedToUser().getFullName(), request.getCompletionNotes())
        );

        return taskMapper.toResponse(saved);
    }

    @Transactional
    public TicketTaskResponse returnTask(Long taskId, ReturnTaskRequest request) {
        TicketTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        task.setStatus(TaskStatus.RETURNED);
        task.setCompletedAt(Instant.now());
        task.setCompletionNotes("RETURNED: " + request.getReason());
        TicketTask saved = taskRepository.save(task);

        Ticket ticket = task.getTicket();
        // Hand back current assignee to delegator
        ticket.setCurrentAssignee(task.getAssignedFromUser());
        ticket.setDelegatedFrom(task.getParentTask() != null ? task.getParentTask().getAssignedFromUser() : null);
        ticketRepository.save(ticket);

        auditService.recordEvent(
                ticket,
                HistoryEventType.TASK_RETURNED,
                currentUser,
                task.getAssignedToUser().getFullName(),
                task.getAssignedFromUser().getFullName(),
                String.format("Task returned by %s to %s. Reason: %s",
                        task.getAssignedToUser().getFullName(), task.getAssignedFromUser().getFullName(), request.getReason()),
                "{\"taskId\":" + taskId + ",\"reason\":\"" + request.getReason() + "\"}"
        );

        notificationService.sendNotification(
                task.getAssignedFromUser(),
                ticket,
                NotificationType.TASK_RETURNED,
                "Task Returned: Ticket #" + ticket.getTicketNumber(),
                String.format("%s returned the task on Ticket #%s: %s",
                        task.getAssignedToUser().getFullName(), ticket.getTicketNumber(), request.getReason())
        );

        return taskMapper.toResponse(saved);
    }

    @Transactional
    public TicketTaskResponse overrideDelegation(Long taskId, OverrideTaskRequest request) {
        TicketTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        User newAssignee = userRepository.findById(request.getAssignToUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssignToUserId()));

        if (!newAssignee.isActive()) {
            throw new DelegationException("Cannot override task to inactive user", ErrorCode.USER_NOT_ACTIVE);
        }

        task.setStatus(TaskStatus.CANCELLED);
        task.setCompletionNotes("OVERRIDDEN BY ADMIN: " + request.getReason());
        taskRepository.save(task);

        Ticket ticket = task.getTicket();
        ticket.setCurrentAssignee(newAssignee);
        ticketRepository.save(ticket);

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        auditService.recordEvent(
                ticket,
                HistoryEventType.REASSIGNED,
                currentUser,
                task.getAssignedToUser().getFullName(),
                newAssignee.getFullName(),
                "Admin overridden task delegation. Reason: " + request.getReason(),
                "{\"taskId\":" + taskId + ",\"reason\":\"" + request.getReason() + "\"}"
        );

        return taskMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TicketTaskResponse> getTasksForTicket(Long ticketId) {
        List<TicketTask> tasks = taskRepository.findByTicketIdOrderByDelegationLevelAscCreatedAtAsc(ticketId);
        return tasks.stream().map(taskMapper::toResponse).toList();
    }

    private boolean isCircularDelegation(TicketTask currentTask, Long targetUserId, User ticketOwner) {
        Set<Long> ancestorIds = new HashSet<>();

        TicketTask pointer = currentTask;
        while (pointer != null) {
            ancestorIds.add(pointer.getAssignedToUser().getId());
            ancestorIds.add(pointer.getAssignedFromUser().getId());
            pointer = pointer.getParentTask();
        }

        return ancestorIds.contains(targetUserId);
    }
}
