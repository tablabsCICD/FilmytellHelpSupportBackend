package com.filmytell.support.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filmytell.support.dto.ticket.ExternalCreateTicketRequest;
import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TicketPriority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExternalTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("External Ticket Ingestion: Missing API Key returns 403 Forbidden")
    void testExternalTicket_WithoutApiKey_Forbidden() throws Exception {
        ExternalCreateTicketRequest request = ExternalCreateTicketRequest.builder()
                .sourcePortal(SourcePortal.FILMYTELL)
                .externalTicketId("EXT-999")
                .customerName("Portal User")
                .customerEmail("user@filmytell.com")
                .category("ACCOUNT_LOGIN")
                .subject("Login issue")
                .description("Cannot sign in")
                .priority(TicketPriority.MEDIUM)
                .build();

        mockMvc.perform(post("/api/v1/external/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
