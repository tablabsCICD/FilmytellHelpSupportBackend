package com.filmytell.support.dto.ticket;

import com.filmytell.support.enums.TicketPriority;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketPriorityHistoryDto {

    private Long id;
    private TicketPriority oldPriority;
    private TicketPriority newPriority;
    private Long changedById;
    private String changedByName;
    private String changeReason;
    private Instant createdAt;
}
