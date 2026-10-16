package com.filmytell.support.dto.sla;

import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TicketPriority;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaPolicyResponse {

    private Long id;
    private String name;
    private TicketPriority priority;
    private SourcePortal sourcePortal;
    private int firstResponseTimeMinutes;
    private int resolutionTimeMinutes;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
