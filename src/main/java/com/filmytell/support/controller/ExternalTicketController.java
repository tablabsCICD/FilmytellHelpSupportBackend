package com.filmytell.support.controller;

import com.filmytell.support.dto.common.ApiResponse;
import com.filmytell.support.dto.conversation.SendMessageRequest;
import com.filmytell.support.dto.conversation.TicketMessageResponse;
import com.filmytell.support.dto.ticket.ExternalCreateTicketRequest;
import com.filmytell.support.dto.ticket.TicketDetailResponse;
import com.filmytell.support.dto.ticket.TicketResponse;
import com.filmytell.support.enums.SenderType;
import com.filmytell.support.service.ConversationService;
import com.filmytell.support.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/external/tickets")
@RequiredArgsConstructor
@Tag(name = "External Portals Integration", description = "Secure APIs for Filmytell, Filmytell Partner, and Production House portals (Authenticated via X-API-KEY)")
public class ExternalTicketController {

    private final TicketService ticketService;
    private final ConversationService conversationService;

    @PostMapping
    @Operation(
            summary = "Create support ticket from external portal",
            description = "Creates a new ticket centrally with idempotency validation using (sourcePortal + externalTicketId)"
    )
    public ResponseEntity<ApiResponse<TicketResponse>> createExternalTicket(
            @Valid @RequestBody ExternalCreateTicketRequest request
    ) {
        TicketResponse response = ticketService.createExternalTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Ticket received and queued for processing", response));
    }

    @GetMapping("/{ticketNumber}/status")
    @Operation(summary = "Check ticket status by ticket number for portal customers", description = "Returns customer-visible ticket details")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> getTicketStatus(
            @PathVariable String ticketNumber
    ) {
        TicketDetailResponse response = ticketService.getTicketByNumber(ticketNumber);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{ticketNumber}/messages")
    @Operation(summary = "Post customer reply to existing ticket", description = "Allows end-users to post replies through their portal")
    public ResponseEntity<ApiResponse<TicketMessageResponse>> postCustomerMessage(
            @PathVariable String ticketNumber,
            @Valid @RequestBody SendMessageRequest request
    ) {
        TicketDetailResponse ticket = ticketService.getTicketByNumber(ticketNumber);
        request.setSenderType(SenderType.USER);
        request.setInternal(false);
        TicketMessageResponse response = conversationService.sendMessage(ticket.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Message posted successfully", response));
    }
}
