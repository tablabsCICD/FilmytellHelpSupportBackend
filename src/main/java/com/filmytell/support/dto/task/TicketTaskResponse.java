package com.filmytell.support.dto.task;

import com.filmytell.support.enums.DelegationReason;
import com.filmytell.support.enums.TaskStatus;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTaskResponse {

    private Long id;
    private Long ticketId;
    private Long parentTaskId;

    private Long assignedFromUserId;
    private String assignedFromUserName;

    private Long assignedToUserId;
    private String assignedToUserName;

    private int delegationLevel;
    private DelegationReason reason;
    private String instructions;
    private TaskStatus status;

    private Instant dueAt;
    private Instant startedAt;
    private Instant completedAt;

    private Long completedByUserId;
    private String completedByUserName;
    private String completionNotes;

    private Instant createdAt;
    private Instant updatedAt;

    private List<TicketTaskResponse> childTasks;
}
