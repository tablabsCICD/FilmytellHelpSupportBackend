package com.filmytell.support.dto.task;

import com.filmytell.support.enums.DelegationReason;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DelegateTaskRequest {

    @NotNull(message = "Target user ID to delegate task to is required")
    private Long assignToUserId;

    @NotNull(message = "Delegation reason is required")
    private DelegationReason reason;

    @NotBlank(message = "Instructions are required")
    private String instructions;

    private Instant dueAt;
}
