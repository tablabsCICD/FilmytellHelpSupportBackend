package com.filmytell.support.dto.ticket;

import javax.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignTicketRequest {

    @NotNull(message = "Assignee user ID is required")
    private Long assigneeId;

    private Long teamId;

    private String reason;
}
