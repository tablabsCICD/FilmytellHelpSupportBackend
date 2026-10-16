package com.filmytell.support.repository;

import com.filmytell.support.entity.SlaPolicy;
import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TicketPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long> {
    Optional<SlaPolicy> findByPriorityAndSourcePortalAndActiveTrue(TicketPriority priority, SourcePortal sourcePortal);
    Optional<SlaPolicy> findByPriorityAndSourcePortalIsNullAndActiveTrue(TicketPriority priority);
}
