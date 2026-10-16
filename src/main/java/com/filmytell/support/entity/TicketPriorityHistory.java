package com.filmytell.support.entity;

import com.filmytell.support.enums.TicketPriority;
import javax.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "ticket_priority_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketPriorityHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_priority", nullable = false, length = 30)
    private TicketPriority oldPriority;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_priority", nullable = false, length = 30)
    private TicketPriority newPriority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private User changedByUser;

    @Column(name = "change_reason")
    private String changeReason;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
