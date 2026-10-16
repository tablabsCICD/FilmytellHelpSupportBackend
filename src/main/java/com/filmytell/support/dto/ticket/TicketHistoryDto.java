package com.filmytell.support.dto.ticket;

import com.filmytell.support.enums.HistoryEventType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketHistoryDto {

    private Long id;
    private HistoryEventType eventType;
    private Long performedById;
    private String performedByName;
    private String oldValue;
    private String newValue;
    private String description;
    private String metadata;
    private Instant createdAt;
}
