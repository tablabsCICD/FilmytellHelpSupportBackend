package com.filmytell.support.entity;

import com.filmytell.support.enums.AiDecision;
import javax.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "ai_interactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(name = "intent_detected", length = 100)
    private String intentDetected;

    @Column(name = "category_detected", length = 100)
    private String categoryDetected;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 50)
    private AiDecision decision;

    @Column(name = "reason")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "knowledge_source_id")
    private KnowledgeArticle knowledgeSource;

    @Column(name = "ai_response_text", columnDefinition = "TEXT")
    private String aiResponseText;

    @Column(name = "model_used", length = 100)
    private String modelUsed;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
