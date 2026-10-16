package com.filmytell.support.controller;

import com.filmytell.support.dto.common.ApiResponse;
import com.filmytell.support.dto.conversation.*;
import com.filmytell.support.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}")
@RequiredArgsConstructor
@Tag(name = "Conversation & Internal Notes", description = "Endpoints for ticket messages and confidential internal agent notes")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping("/messages")
    @PreAuthorize("hasAnyAuthority('MESSAGE_SEND', 'ROLE_ADMIN', 'ROLE_SUPPORT_MEMBER', 'ROLE_TECHNICAL_SUPPORT_MEMBER')")
    @Operation(summary = "Send message in ticket conversation")
    public ResponseEntity<ApiResponse<TicketMessageResponse>> sendMessage(
            @PathVariable Long ticketId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        TicketMessageResponse response = conversationService.sendMessage(ticketId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Message sent successfully", response));
    }

    @GetMapping("/messages")
    @PreAuthorize("hasAnyAuthority('TICKET_VIEW', 'ROLE_ADMIN')")
    @Operation(summary = "Get conversation messages for ticket")
    public ResponseEntity<ApiResponse<List<TicketMessageResponse>>> getMessages(
            @PathVariable Long ticketId,
            @RequestParam(defaultValue = "true") boolean includeInternal
    ) {
        List<TicketMessageResponse> messages = conversationService.getMessages(ticketId, includeInternal);
        return ResponseEntity.ok(ApiResponse.ok(messages));
    }

    @PostMapping("/notes")
    @PreAuthorize("hasAnyAuthority('NOTE_ADD', 'ROLE_ADMIN', 'ROLE_SUPPORT_MEMBER', 'ROLE_TECHNICAL_SUPPORT_MEMBER')")
    @Operation(summary = "Add internal note to ticket (Hidden from customers)")
    public ResponseEntity<ApiResponse<TicketNoteResponse>> addInternalNote(
            @PathVariable Long ticketId,
            @Valid @RequestBody AddNoteRequest request
    ) {
        TicketNoteResponse response = conversationService.addInternalNote(ticketId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Internal note added successfully", response));
    }

    @GetMapping("/notes")
    @PreAuthorize("hasAnyAuthority('TICKET_VIEW', 'ROLE_ADMIN', 'ROLE_SUPPORT_MEMBER', 'ROLE_TECHNICAL_SUPPORT_MEMBER')")
    @Operation(summary = "Get internal notes for ticket")
    public ResponseEntity<ApiResponse<List<TicketNoteResponse>>> getInternalNotes(@PathVariable Long ticketId) {
        List<TicketNoteResponse> notes = conversationService.getInternalNotes(ticketId);
        return ResponseEntity.ok(ApiResponse.ok(notes));
    }
}
