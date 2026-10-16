package com.filmytell.support.dto.task;

import com.filmytell.support.enums.TaskStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DelegationChainNodeDto {

    private int level;
    private Long taskId;
    private Long fromUserId;
    private String fromUserName;
    private Long toUserId;
    private String toUserName;
    private TaskStatus status;
    private String reason;
    private Instant createdAt;
    private Instant completedAt;
}
