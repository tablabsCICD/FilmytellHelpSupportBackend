package com.filmytell.support.service;

import com.filmytell.support.entity.*;
import com.filmytell.support.enums.*;
import com.filmytell.support.repository.*;
import com.filmytell.support.service.ai.AiDecisionEngine;
import com.filmytell.support.service.ai.AiSupportService;
import com.filmytell.support.service.ai.KnowledgeSearchEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiSupportServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private KnowledgeSearchEngine searchEngine;

    @Spy
    private AiDecisionEngine decisionEngine = new AiDecisionEngine();

    @Mock
    private AiInteractionRepository aiInteractionRepository;

    @Mock
    private TicketMessageRepository messageRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AiSupportService aiSupportService;

    private Ticket faqTicket;
    private Ticket sensitiveTicket;
    private KnowledgeArticle passwordArticle;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(decisionEngine, "defaultConfidenceThreshold", 0.85);
        ReflectionTestUtils.setField(aiSupportService, "aiEnabled", true);
        ReflectionTestUtils.setField(aiSupportService, "aiModelName", "gemini-1.5-pro");

        faqTicket = Ticket.builder()
                .id(200L)
                .ticketNumber("FLM-2001")
                .sourcePortal(SourcePortal.FILMYTELL)
                .customerName("John Doe")
                .category("ACCOUNT_LOGIN")
                .subject("How do I reset password")
                .description("I forgot my password and want to reset it")
                .status(TicketStatus.NEW)
                .aiEligible(true)
                .build();

        sensitiveTicket = Ticket.builder()
                .id(201L)
                .ticketNumber("FLM-2002")
                .sourcePortal(SourcePortal.FILMYTELL)
                .customerName("Jane Doe")
                .category("REFUND")
                .subject("Immediate Refund Request")
                .description("Card was charged twice, I demand an immediate refund")
                .status(TicketStatus.NEW)
                .aiEligible(true)
                .build();

        passwordArticle = KnowledgeArticle.builder()
                .id(1L)
                .title("Password Reset Guide")
                .category("ACCOUNT_LOGIN")
                .content("Click on 'Forgot Password' on the login screen.")
                .status(KnowledgeStatus.PUBLISHED)
                .build();
    }

    @Test
    @DisplayName("AI Auto-Resolution: High confidence knowledge grounding marks ticket AI_RESOLVED")
    void testAiAutoResolution_Success() {
        when(ticketRepository.findById(200L)).thenReturn(Optional.of(faqTicket));
        when(searchEngine.findBestMatchingArticle(any(), any(), any())).thenReturn(Optional.of(passwordArticle));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        aiSupportService.analyzeAndResolveTicket(200L);

        assertEquals(TicketStatus.AI_RESOLVED, faqTicket.getStatus());
        assertTrue(faqTicket.isAiResolved());
        assertNotNull(faqTicket.getAiConfidence());
        assertTrue(faqTicket.getAiConfidence() >= 0.85);
        verify(messageRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("AI Sensitive Category Escalation: Refund request escalates to human support")
    void testAiSensitiveCategory_EscalatesToHuman() {
        when(ticketRepository.findById(201L)).thenReturn(Optional.of(sensitiveTicket));
        when(searchEngine.findBestMatchingArticle(any(), any(), any())).thenReturn(Optional.empty());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        aiSupportService.analyzeAndResolveTicket(201L);

        assertEquals(TicketStatus.NEW, sensitiveTicket.getStatus());
        assertFalse(sensitiveTicket.isAiResolved());
        verify(messageRepository, never()).save(any());
        verify(aiInteractionRepository, times(1)).save(any());
    }
}
