package com.filmytell.support.service;

import com.filmytell.support.dto.conversation.*;
import com.filmytell.support.entity.*;
import com.filmytell.support.enums.*;
import com.filmytell.support.exception.ResourceNotFoundException;
import com.filmytell.support.mapper.ConversationMapper;
import com.filmytell.support.repository.*;
import com.filmytell.support.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.filmytell.support.enums.TicketStatus;
import com.filmytell.support.service.integration.AdminPortalTicketClient;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final TicketNoteRepository noteRepository;
    private final TicketAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final ConversationMapper conversationMapper;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final AdminPortalTicketClient adminPortalTicketClient;

    @Transactional
    public TicketMessageResponse sendMessage(Long ticketId, SendMessageRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        SenderType senderType = request.getSenderType() != null ? request.getSenderType()
                : (currentUser != null ? SenderType.SUPPORT : SenderType.USER);

        TicketMessage message = TicketMessage.builder()
                .ticket(ticket)
                .sender(currentUser)
                .senderName(request.getSenderName() != null ? request.getSenderName()
                        : (currentUser != null ? currentUser.getFullName() : ticket.getCustomerName()))
                .senderType(senderType)
                .message(request.getMessage())
                .messageType(request.getMessageType() != null ? request.getMessageType() : MessageType.TEXT)
                .internal(request.isInternal())
                .build();

        TicketMessage saved = messageRepository.save(message);

        // Sync customer reply back to Admin Portal if ticket originated from Admin Portal
        if (senderType == SenderType.SUPPORT && !request.isInternal() 
                && "ADMIN_PORTAL".equalsIgnoreCase(ticket.getSourceSystem()) 
                && ticket.getSourceTicketId() != null) {
            boolean isResolved = (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED);
            adminPortalTicketClient.updateTicketOnAdminPortal(
                    ticket.getSourceTicketId(),
                    request.getMessage(),
                    isResolved
            );
        }

        // Record first response timestamp for SLA
        if (senderType == SenderType.SUPPORT && ticket.getFirstRespondedAt() == null) {
            ticket.setFirstRespondedAt(Instant.now());
            ticketRepository.save(ticket);
        }

        auditService.recordEvent(
                ticket,
                HistoryEventType.MESSAGE_SENT,
                currentUser,
                null,
                senderType.name(),
                "Message sent (" + senderType + "): " + abbreviate(request.getMessage(), 60),
                "{\"messageId\":" + saved.getId() + ",\"type\":\"" + senderType + "\"}"
        );

        return conversationMapper.toMessageResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TicketMessageResponse> getMessages(Long ticketId, boolean includeInternal) {
        List<TicketMessage> messages = includeInternal
                ? messageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                : messageRepository.findByTicketIdAndInternalFalseOrderByCreatedAtAsc(ticketId);

        return messages.stream().map(conversationMapper::toMessageResponse).toList();
    }

    @Transactional
    public TicketNoteResponse addInternalNote(Long ticketId, AddNoteRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null
                ? userRepository.findById(currentUserId).orElseThrow(() -> new ResourceNotFoundException("User not found"))
                : userRepository.findAll().get(0);

        TicketNote note = TicketNote.builder()
                .ticket(ticket)
                .author(currentUser)
                .note(request.getNote())
                .build();

        TicketNote saved = noteRepository.save(note);

        auditService.recordEvent(
                ticket,
                HistoryEventType.NOTE_ADDED,
                currentUser,
                null,
                null,
                "Internal note added by " + currentUser.getFullName(),
                "{\"noteId\":" + saved.getId() + "}"
        );

        return conversationMapper.toNoteResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TicketNoteResponse> getInternalNotes(Long ticketId) {
        return noteRepository.findByTicketIdOrderByCreatedAtDesc(ticketId).stream()
                .map(conversationMapper::toNoteResponse)
                .toList();
    }

    private String abbreviate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen) + "..." : str;
    }
}
