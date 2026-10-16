package com.filmytell.support.mapper;

import com.filmytell.support.dto.task.DelegationChainNodeDto;
import com.filmytell.support.dto.task.TicketTaskResponse;
import com.filmytell.support.entity.TicketTask;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class TaskMapper {

    public TicketTaskResponse toResponse(TicketTask task) {
        if (task == null) return null;

        List<TicketTaskResponse> childDtos = task.getChildTasks() != null
                ? task.getChildTasks().stream().map(this::toResponse).toList()
                : Collections.emptyList();

        return TicketTaskResponse.builder()
                .id(task.getId())
                .ticketId(task.getTicket().getId())
                .parentTaskId(task.getParentTask() != null ? task.getParentTask().getId() : null)
                .assignedFromUserId(task.getAssignedFromUser().getId())
                .assignedFromUserName(task.getAssignedFromUser().getFullName())
                .assignedToUserId(task.getAssignedToUser().getId())
                .assignedToUserName(task.getAssignedToUser().getFullName())
                .delegationLevel(task.getDelegationLevel())
                .reason(task.getReason())
                .instructions(task.getInstructions())
                .status(task.getStatus())
                .dueAt(task.getDueAt())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .completedByUserId(task.getCompletedByUser() != null ? task.getCompletedByUser().getId() : null)
                .completedByUserName(task.getCompletedByUser() != null ? task.getCompletedByUser().getFullName() : null)
                .completionNotes(task.getCompletionNotes())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .childTasks(childDtos)
                .build();
    }

    public DelegationChainNodeDto toChainNode(TicketTask task) {
        if (task == null) return null;

        return DelegationChainNodeDto.builder()
                .level(task.getDelegationLevel())
                .taskId(task.getId())
                .fromUserId(task.getAssignedFromUser().getId())
                .fromUserName(task.getAssignedFromUser().getFullName())
                .toUserId(task.getAssignedToUser().getId())
                .toUserName(task.getAssignedToUser().getFullName())
                .status(task.getStatus())
                .reason(task.getReason() != null ? task.getReason().name() : null)
                .createdAt(task.getCreatedAt())
                .completedAt(task.getCompletedAt())
                .build();
    }
}
