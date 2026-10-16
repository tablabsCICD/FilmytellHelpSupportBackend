package com.filmytell.support.service.ai;

import com.filmytell.support.entity.*;
import com.filmytell.support.enums.*;
import com.filmytell.support.repository.*;
import com.filmytell.support.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSupportService {

    private final TicketRepository ticketRepository;
    private final KnowledgeSearchEngine searchEngine;
    private final AiDecisionEngine decisionEngine;
    private final AiInteractionRepository aiInteractionRepository;
    private final TicketMessageRepository messageRepository;
    private final AuditService auditService;

    @Value("${app.ai.enabled:true}")
    private boolean aiEnabled;

    @Value("${app.ai.model:gemini-1.5-pro}")
    private String aiModelName;

    @Async("aiTaskExecutor")
    @Transactional
    public void processTicketAsync(Long ticketId) {
        if (!aiEnabled) {
            log.info("AI processing skipped (disabled in config)");
            return;
        }

        try {
            analyzeAndResolveTicket(ticketId);
        } catch (Exception ex) {
            log.error("AI analysis encountered an error for ticket ID {}. Falling back to human workflow: {}", ticketId, ex.getMessage());
        }
    }

    @Transactional
    public void analyzeAndResolveTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket == null || !ticket.isAiEligible()) {
            return;
        }

        // Set status to AI_ANALYSIS if currently NEW
        if (ticket.getStatus() == TicketStatus.NEW) {
            ticket.setStatus(TicketStatus.AI_ANALYSIS);
            ticketRepository.save(ticket);
        }

        Optional<KnowledgeArticle> matchedArticle = searchEngine.findBestMatchingArticle(
                ticket.getSubject(), ticket.getDescription(), ticket.getCategory()
        );

        AiDecisionEngine.AiAnalysisResult result = decisionEngine.evaluate(ticket, matchedArticle.orElse(null));

        // Save AI interaction log
        AiInteraction interaction = AiInteraction.builder()
                .ticket(ticket)
                .intentDetected(result.getIntent())
                .categoryDetected(result.getCategory())
                .confidenceScore(result.getConfidence())
                .decision(result.getDecision())
                .reason(result.getReason())
                .knowledgeSource(result.getKnowledgeSource())
                .aiResponseText(result.getGeneratedResponse())
                .modelUsed(aiModelName)
                .build();
        aiInteractionRepository.save(interaction);

        ticket.setAiConfidence(result.getConfidence());

        if (result.getDecision() == AiDecision.AUTO_RESOLVE && result.getGeneratedResponse() != null) {
            ticket.setStatus(TicketStatus.AI_RESOLVED);
            ticket.setAiResolved(true);
            ticketRepository.save(ticket);

            // Post AI generated response to conversation
            TicketMessage message = TicketMessage.builder()
                    .ticket(ticket)
                    .sender(null)
                    .senderName("Filmytell AI Assistant")
                    .senderType(SenderType.AI)
                    .message(result.getGeneratedResponse())
                    .messageType(MessageType.TEXT)
                    .internal(false)
                    .build();
            messageRepository.save(message);

            auditService.recordEvent(
                    ticket,
                    HistoryEventType.AI_RESPONSE,
                    null,
                    TicketStatus.AI_ANALYSIS.name(),
                    TicketStatus.AI_RESOLVED.name(),
                    "AI automatically resolved ticket using knowledge base article: "
                            + (result.getKnowledgeSource() != null ? result.getKnowledgeSource().getTitle() : "General"),
                    "{\"confidence\":" + result.getConfidence() + ",\"decision\":\"AUTO_RESOLVE\"}"
            );

            log.info("Ticket #{} successfully AUTO_RESOLVED by AI with confidence {}", ticket.getTicketNumber(), result.getConfidence());
        } else {
            // Escalate to human support team
            ticket.setStatus(ticket.getCurrentAssignee() != null ? TicketStatus.ASSIGNED : TicketStatus.NEW);
            ticket.setAiResolved(false);
            ticketRepository.save(ticket);

            auditService.recordEvent(
                    ticket,
                    HistoryEventType.AI_ESCALATED,
                    null,
                    TicketStatus.AI_ANALYSIS.name(),
                    ticket.getStatus().name(),
                    "AI evaluated ticket and escalated to human support. Reason: " + result.getReason(),
                    "{\"confidence\":" + result.getConfidence() + ",\"decision\":\"ESCALATE_TO_HUMAN\"}"
            );

            log.info("Ticket #{} escalated by AI to human support: {}", ticket.getTicketNumber(), result.getReason());
        }
    }
}
