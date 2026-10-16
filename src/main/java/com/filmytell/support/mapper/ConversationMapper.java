package com.filmytell.support.mapper;

import com.filmytell.support.dto.conversation.TicketAttachmentResponse;
import com.filmytell.support.dto.conversation.TicketMessageResponse;
import com.filmytell.support.dto.conversation.TicketNoteResponse;
import com.filmytell.support.entity.TicketAttachment;
import com.filmytell.support.entity.TicketMessage;
import com.filmytell.support.entity.TicketNote;
import org.springframework.stereotype.Component;

@Component
public class ConversationMapper {

    public TicketMessageResponse toMessageResponse(TicketMessage message) {
        if (message == null) return null;

        return TicketMessageResponse.builder()
                .id(message.getId())
                .ticketId(message.getTicket().getId())
                .senderId(message.getSender() != null ? message.getSender().getId() : null)
                .senderName(message.getSenderName() != null ? message.getSenderName()
                        : (message.getSender() != null ? message.getSender().getFullName() : "SYSTEM"))
                .senderType(message.getSenderType())
                .message(message.getMessage())
                .messageType(message.getMessageType())
                .isInternal(message.isInternal())
                .createdAt(message.getCreatedAt())
                .build();
    }

    public TicketNoteResponse toNoteResponse(TicketNote note) {
        if (note == null) return null;

        return TicketNoteResponse.builder()
                .id(note.getId())
                .ticketId(note.getTicket().getId())
                .authorId(note.getAuthor().getId())
                .authorName(note.getAuthor().getFullName())
                .note(note.getNote())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }

    public TicketAttachmentResponse toAttachmentResponse(TicketAttachment attachment) {
        if (attachment == null) return null;

        return TicketAttachmentResponse.builder()
                .id(attachment.getId())
                .ticketId(attachment.getTicket().getId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .fileUrl(attachment.getFileUrl())
                .uploadedById(attachment.getUploadedByUser() != null ? attachment.getUploadedByUser().getId() : null)
                .uploadedByName(attachment.getUploadedByUser() != null ? attachment.getUploadedByUser().getFullName() : "SYSTEM")
                .isInternal(attachment.isInternal())
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
