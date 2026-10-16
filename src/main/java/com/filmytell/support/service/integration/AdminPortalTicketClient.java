package com.filmytell.support.service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.filmytell.support.dto.adminportal.AdminPortalTicketDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPortalTicketClient {

    private final RestTemplate restTemplate;

    @Value("${admin.portal.ticket-api-url:https://filmytell.com/ott/api/TicketRaised/getAllTicketRaised}")
    private String apiUrl;

    @Value("${admin.portal.update-ticket-api-url:https://filmytell.com/ott/api/TicketRaised/updateTicket}")
    private String updateApiUrl;

    @Value("${admin.portal.api-token:}")
    private String apiToken;

    public boolean updateTicketOnAdminPortal(Object tickedId, String feedbackMessage, boolean isResolved) {
        log.info("[Ticket Sync] Sending update/reply to Admin Portal API for ticket {}: resolved={}, feedback={}", tickedId, isResolved, feedbackMessage);
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            if (apiToken != null && !apiToken.isBlank()) {
                headers.set("Authorization", apiToken.startsWith("Bearer ") ? apiToken : "Bearer " + apiToken);
            }

            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("tickedId", tickedId);
            body.put("feedback", feedbackMessage != null ? feedbackMessage : "");
            body.put("isResolved", isResolved);

            org.springframework.http.HttpEntity<java.util.Map<String, Object>> request = new org.springframework.http.HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(updateApiUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[Ticket Sync] Successfully updated ticket {} on Admin Portal", tickedId);
                return true;
            } else {
                log.warn("[Ticket Sync] Admin Portal update returned status {}: {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("[Ticket Sync] Failed to update ticket {} on Admin Portal: {}", tickedId, e.getMessage());
        }
        return false;
    }

    public List<AdminPortalTicketDto> fetchAllTickets() {
        log.info("[Ticket Sync] Calling Admin Portal API: {}", apiUrl);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.getBody());
                JsonNode dataNode = rootNode.has("data") ? rootNode.get("data") : rootNode;

                List<AdminPortalTicketDto> tickets = new ArrayList<>();

                if (dataNode.has("TicketRaised") && dataNode.get("TicketRaised").isArray()) {
                    for (JsonNode node : dataNode.get("TicketRaised")) {
                        AdminPortalTicketDto ticket = mapper.treeToValue(node, AdminPortalTicketDto.class);
                        if (ticket != null && ticket.getSourceTicketId() != null) {
                            tickets.add(ticket);
                        }
                    }
                } else if (dataNode.has("ticketRaised") && dataNode.get("ticketRaised").isArray()) {
                    for (JsonNode node : dataNode.get("ticketRaised")) {
                        AdminPortalTicketDto ticket = mapper.treeToValue(node, AdminPortalTicketDto.class);
                        if (ticket != null && ticket.getSourceTicketId() != null) {
                            tickets.add(ticket);
                        }
                    }
                } else if (dataNode.isArray()) {
                    for (JsonNode node : dataNode) {
                        AdminPortalTicketDto ticket = mapper.treeToValue(node, AdminPortalTicketDto.class);
                        if (ticket != null && ticket.getSourceTicketId() != null) {
                            tickets.add(ticket);
                        }
                    }
                }

                log.info("[Ticket Sync] Fetched {} tickets from Admin Portal", tickets.size());
                return tickets;
            } else {
                log.warn("[Ticket Sync] Admin Portal API returned non-success status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("[Ticket Sync] Failed to fetch tickets from Admin Portal API ({}): {}", apiUrl, e.getMessage());
        }
        return Collections.emptyList();
    }
}
