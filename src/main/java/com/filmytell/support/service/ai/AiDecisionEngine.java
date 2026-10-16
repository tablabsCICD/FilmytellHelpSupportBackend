package com.filmytell.support.service.ai;

import com.filmytell.support.entity.KnowledgeArticle;
import com.filmytell.support.entity.Ticket;
import com.filmytell.support.enums.AiDecision;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class AiDecisionEngine {

    @Value("${app.ai.confidence-threshold:0.85}")
    private double defaultConfidenceThreshold;

    private static final Set<String> SENSITIVE_CATEGORIES = Set.of(
            "REFUND", "FRAUD", "SECURITY", "ACCOUNT_OWNERSHIP", "LEGAL", "SENSITIVE_PERSONAL_DATA", "BILLING"
    );

    @Getter
    @Builder
    public static class AiAnalysisResult {
        private String intent;
        private String category;
        private double confidence;
        private AiDecision decision;
        private String reason;
        private KnowledgeArticle knowledgeSource;
        private String generatedResponse;
    }

    public AiAnalysisResult evaluate(Ticket ticket, KnowledgeArticle article) {
        String category = ticket.getCategory() != null ? ticket.getCategory().toUpperCase() : "GENERAL";
        String subject = ticket.getSubject().toLowerCase();
        String description = ticket.getDescription().toLowerCase();

        // 1. Check if category or intent is strictly sensitive -> ESCALATE TO HUMAN immediately
        if (SENSITIVE_CATEGORIES.contains(category) || isSensitiveContent(subject, description)) {
            return AiAnalysisResult.builder()
                    .intent("SENSITIVE_REQUEST")
                    .category(category)
                    .confidence(0.95)
                    .decision(AiDecision.ESCALATE_TO_HUMAN)
                    .reason("Sensitive category/content detected (Refund/Fraud/Security). Escalated to human support.")
                    .knowledgeSource(null)
                    .generatedResponse(null)
                    .build();
        }

        // 2. Check if a high-quality knowledge article is available
        if (article != null) {
            double calculatedConfidence = 0.92; // High confidence grounded in published knowledge
            if (calculatedConfidence >= defaultConfidenceThreshold) {
                String aiAnswer = "Hello " + (ticket.getCustomerName() != null ? ticket.getCustomerName() : "Valued Customer") + ",\n\n"
                        + "Here is the recommended solution for your issue:\n\n"
                        + article.getContent() + "\n\n"
                        + "If this resolves your issue, please reply with 'Resolved'. Otherwise, a human agent will assist you shortly.\n\n"
                        + "Best regards,\nFilmytell Automated Support Assistant";

                return AiAnalysisResult.builder()
                        .intent("RESOLVE_VIA_KNOWLEDGE_BASE")
                        .category(category)
                        .confidence(calculatedConfidence)
                        .decision(AiDecision.AUTO_RESOLVE)
                        .reason("High confidence resolution found in published Knowledge Base article: " + article.getTitle())
                        .knowledgeSource(article)
                        .generatedResponse(aiAnswer)
                        .build();
            }
        }

        // 3. Fallback: Confidence is lower than threshold -> Escalate to human support
        return AiAnalysisResult.builder()
                .intent("GENERAL_QUERY")
                .category(category)
                .confidence(0.60)
                .decision(AiDecision.ESCALATE_TO_HUMAN)
                .reason("Confidence score (0.60) is below the threshold (" + defaultConfidenceThreshold + "). Forwarding to support team.")
                .knowledgeSource(null)
                .generatedResponse(null)
                .build();
    }

    private boolean isSensitiveContent(String subject, String description) {
        String combined = subject + " " + description;
        return combined.contains("refund")
                || combined.contains("money back")
                || combined.contains("hack")
                || combined.contains("fraud")
                || combined.contains("stolen")
                || combined.contains("legal action")
                || combined.contains("credit card charged wrongly");
    }
}
