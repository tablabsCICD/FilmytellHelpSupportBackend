package com.filmytell.support.entity;

import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TicketPriority;
import com.filmytell.support.enums.TicketStatus;
import javax.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_source_external", columnNames = {"source_portal", "external_ticket_id"}),
        @UniqueConstraint(name = "uk_source_system_ticket_id", columnNames = {"source_system", "source_ticket_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_number", nullable = false, unique = true, length = 50)
    private String ticketNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_portal", nullable = false, length = 50)
    private SourcePortal sourcePortal;

    @Column(name = "external_ticket_id", length = 100)
    private String externalTicketId;

    @Column(name = "source_system", length = 50)
    private String sourceSystem;

    @Column(name = "source_ticket_id", length = 100)
    private String sourceTicketId;

    @Column(name = "customer_id", length = 100)
    private String customerId;

    @Column(name = "customer_name", length = 150)
    private String customerName;

    @Column(name = "customer_email", length = 150)
    private String customerEmail;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "sub_category", length = 100)
    private String subCategory;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "priority", nullable = false, length = 30)
    private TicketPriority priority = TicketPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private TicketStatus status = TicketStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_owner_id")
    private User ticketOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_assignee_id")
    private User currentAssignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegated_from_id")
    private User delegatedFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_team_id")
    private Team assignedTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sla_policy_id")
    private SlaPolicy slaPolicy;

    @Column(name = "sla_response_due_at")
    private Instant slaResponseDueAt;

    @Column(name = "sla_resolution_due_at")
    private Instant slaResolutionDueAt;

    @Column(name = "first_responded_at")
    private Instant firstRespondedAt;

    @Builder.Default
    @Column(name = "is_sla_breached", nullable = false)
    private boolean slaBreached = false;

    @Builder.Default
    @Column(name = "ai_eligible", nullable = false)
    private boolean aiEligible = true;

    @Column(name = "ai_confidence")
    private Double aiConfidence;

    @Builder.Default
    @Column(name = "ai_resolved", nullable = false)
    private boolean aiResolved = false;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Builder.Default
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("delegationLevel ASC, createdAt ASC")
    private List<TicketTask> tasks = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<TicketMessage> messages = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<TicketNote> notes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<TicketAttachment> attachments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<TicketHistory> histories = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<TicketAssignment> assignments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<TicketPriorityHistory> priorityHistories = new ArrayList<>();
}
