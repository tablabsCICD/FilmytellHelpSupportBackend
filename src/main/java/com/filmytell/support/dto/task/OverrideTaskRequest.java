package com.filmytell.support.dto.task;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverrideTaskRequest {

    @NotNull(message = "Reassignment user ID is required")
    private Long assignToUserId;

    @NotBlank(message = "Override reason is required")
    private String reason;
}
