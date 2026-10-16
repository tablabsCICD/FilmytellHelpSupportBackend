package com.filmytell.support.service;

import com.filmytell.support.dto.notification.NotificationResponse;
import com.filmytell.support.entity.Notification;
import com.filmytell.support.entity.Ticket;
import com.filmytell.support.entity.User;
import com.filmytell.support.enums.NotificationChannel;
import com.filmytell.support.enums.NotificationType;
import com.filmytell.support.exception.ResourceNotFoundException;
import com.filmytell.support.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void sendNotification(
            User recipient,
            Ticket ticket,
            NotificationType type,
            String title,
            String message
    ) {
        if (recipient == null) {
            log.warn("Cannot send notification with null recipient for ticket: {}", ticket != null ? ticket.getTicketNumber() : "N/A");
            return;
        }

        Notification notification = Notification.builder()
                .recipientUser(recipient)
                .ticket(ticket)
                .type(type)
                .channel(NotificationChannel.IN_APP)
                .title(title)
                .message(message)
                .read(false)
                .build();

        notificationRepository.save(notification);
        log.info("Notification sent to {} [{}]: {}", recipient.getUsername(), type, title);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId, boolean unreadOnly) {
        List<Notification> notifications = unreadOnly
                ? notificationRepository.findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                : notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream().map(this::toResponse).toList();
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        if (notification.getRecipientUser().getId().equals(userId)) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .recipientUserId(n.getRecipientUser().getId())
                .ticketId(n.getTicket() != null ? n.getTicket().getId() : null)
                .type(n.getType())
                .channel(n.getChannel())
                .title(n.getTitle())
                .message(n.getMessage())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
