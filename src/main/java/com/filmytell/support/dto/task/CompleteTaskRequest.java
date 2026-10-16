package com.filmytell.support.dto.task;

import javax.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteTaskRequest {

    @NotBlank(message = "Completion notes / resolution explanation is required")
    private String completionNotes;
}
