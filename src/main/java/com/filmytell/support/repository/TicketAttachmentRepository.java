package com.filmytell.support.repository;

import com.filmytell.support.entity.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, Long> {
    List<TicketAttachment> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
    List<TicketAttachment> findByTicketIdAndInternalFalseOrderByCreatedAtDesc(Long ticketId);
}
