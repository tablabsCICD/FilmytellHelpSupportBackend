package com.filmytell.support.repository;

import com.filmytell.support.entity.AiInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiInteractionRepository extends JpaRepository<AiInteraction, Long> {
    List<AiInteraction> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
}
