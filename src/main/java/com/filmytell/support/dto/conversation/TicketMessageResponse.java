package com.filmytell.support.dto.conversation;

import com.filmytell.support.enums.MessageType;
import com.filmytell.support.enums.SenderType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketMessageResponse {

    private Long id;
    private Long ticketId;
    private Long senderId;
    private String senderName;
    private SenderType senderType;
    private String message;
    private MessageType messageType;
    private boolean isInternal;
    private Instant createdAt;
}
