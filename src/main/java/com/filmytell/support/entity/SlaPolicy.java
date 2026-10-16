package com.filmytell.support.entity;

import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TicketPriority;
import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "sla_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaPolicy extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 30)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_portal", length = 50)
    private SourcePortal sourcePortal;

    @Column(name = "first_response_time_minutes", nullable = false)
    private int firstResponseTimeMinutes;

    @Column(name = "resolution_time_minutes", nullable = false)
    private int resolutionTimeMinutes;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
