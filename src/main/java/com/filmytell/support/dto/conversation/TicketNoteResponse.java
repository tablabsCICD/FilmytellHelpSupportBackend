package com.filmytell.support.dto.conversation;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketNoteResponse {

    private Long id;
    private Long ticketId;
    private Long authorId;
    private String authorName;
    private String note;
    private Instant createdAt;
    private Instant updatedAt;
}
