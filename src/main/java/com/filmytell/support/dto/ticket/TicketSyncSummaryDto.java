package com.filmytell.support.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketSyncSummaryDto {
    private boolean success;
    private int totalFetched;
    private int newTickets;
    private int existingTickets;
    private int duplicatesSkipped;
}
