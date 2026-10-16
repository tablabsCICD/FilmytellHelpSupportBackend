package com.filmytell.support.dto.ticket;

import com.filmytell.support.enums.TicketPriority;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePriorityRequest {

    @NotNull(message = "New priority is required")
    private TicketPriority priority;

    @NotBlank(message = "Reason for priority change is required")
    private String reason;
}
