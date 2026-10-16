package com.filmytell.support.dto.task;

import javax.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnTaskRequest {

    @NotBlank(message = "Reason for returning the task is required")
    private String reason;
}
