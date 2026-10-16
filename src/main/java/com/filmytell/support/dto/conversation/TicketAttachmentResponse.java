package com.filmytell.support.dto.conversation;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketAttachmentResponse {

    private Long id;
    private Long ticketId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileUrl;
    private Long uploadedById;
    private String uploadedByName;
    private boolean isInternal;
    private Instant createdAt;
}
