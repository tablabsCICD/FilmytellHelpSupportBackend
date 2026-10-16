package com.filmytell.support.repository;

import com.filmytell.support.entity.Ticket;
import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TicketPriority;
import com.filmytell.support.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    Optional<Ticket> findBySourcePortalAndExternalTicketId(SourcePortal sourcePortal, String externalTicketId);

    boolean existsBySourcePortalAndExternalTicketId(SourcePortal sourcePortal, String externalTicketId);

    Optional<Ticket> findBySourceSystemAndSourceTicketId(String sourceSystem, String sourceTicketId);

    boolean existsBySourceSystemAndSourceTicketId(String sourceSystem, String sourceTicketId);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByStatusIn(List<TicketStatus> statuses);

    List<Ticket> findByCurrentAssigneeId(Long assigneeId);

    List<Ticket> findByTicketOwnerId(Long ownerId);

    List<Ticket> findByAssignedTeamId(Long teamId);

    @Query("SELECT t FROM Ticket t WHERE t.status NOT IN ('RESOLVED', 'CLOSED') AND t.slaResolutionDueAt < :now AND t.slaBreached = false")
    List<Ticket> findPendingSlaBreaches(Instant now);

    @Query("SELECT t.status, COUNT(t) FROM Ticket t GROUP BY t.status")
    List<Object[]> countTicketsByStatus();

    @Query("SELECT t.priority, COUNT(t) FROM Ticket t GROUP BY t.priority")
    List<Object[]> countTicketsByPriority();

    @Query("SELECT t.sourcePortal, COUNT(t) FROM Ticket t GROUP BY t.sourcePortal")
    List<Object[]> countTicketsBySourcePortal();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status NOT IN ('RESOLVED', 'CLOSED')")
    long countOpenTickets();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.slaBreached = true")
    long countSlaBreachedTickets();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.aiResolved = true")
    long countAiResolvedTickets();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.aiConfidence IS NOT NULL AND t.aiResolved = false")
    long countAiEscalatedTickets();

    List<Ticket> findByResolvedAtIsNotNull();

    List<Ticket> findByFirstRespondedAtIsNotNull();
}
