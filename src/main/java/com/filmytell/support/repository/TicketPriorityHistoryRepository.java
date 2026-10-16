package com.filmytell.support.repository;

import com.filmytell.support.entity.TicketPriorityHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketPriorityHistoryRepository extends JpaRepository<TicketPriorityHistory, Long> {
    List<TicketPriorityHistory> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
}
