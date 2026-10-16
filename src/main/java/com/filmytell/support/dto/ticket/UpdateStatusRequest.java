package com.filmytell.support.dto.ticket;

import com.filmytell.support.enums.TicketStatus;
import javax.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStatusRequest {

    @NotNull(message = "Target status is required")
    private TicketStatus status;

    private String reason;
}
