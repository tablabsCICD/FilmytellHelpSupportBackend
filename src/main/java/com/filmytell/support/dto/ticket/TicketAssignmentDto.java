package com.filmytell.support.dto.ticket;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketAssignmentDto {

    private Long id;
    private Long fromUserId;
    private String fromUserName;
    private Long toUserId;
    private String toUserName;
    private Long fromTeamId;
    private String fromTeamName;
    private Long toTeamId;
    private String toTeamName;
    private Long assignedById;
    private String assignedByName;
    private String reason;
    private Instant createdAt;
}
