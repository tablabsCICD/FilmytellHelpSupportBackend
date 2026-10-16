package com.filmytell.support.dto.sla;

import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TicketPriority;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaPolicyRequest {

    @NotBlank(message = "Policy name is required")
    private String name;

    @NotNull(message = "Priority is required")
    private TicketPriority priority;

    private SourcePortal sourcePortal;

    @Min(value = 1, message = "First response time must be at least 1 minute")
    private int firstResponseTimeMinutes;

    @Min(value = 1, message = "Resolution time must be at least 1 minute")
    private int resolutionTimeMinutes;
}
