package com.filmytell.support.dto.notification;

import com.filmytell.support.enums.NotificationChannel;
import com.filmytell.support.enums.NotificationType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private Long recipientUserId;
    private Long ticketId;
    private NotificationType type;
    private NotificationChannel channel;
    private String title;
    private String message;
    private boolean read;
    private Instant createdAt;
}
