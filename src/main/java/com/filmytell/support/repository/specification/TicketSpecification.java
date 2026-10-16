package com.filmytell.support.repository.specification;

import com.filmytell.support.entity.Ticket;
import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TicketPriority;
import com.filmytell.support.enums.TicketStatus;
import javax.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TicketSpecification {

    public static Specification<Ticket> filterTickets(
            String ticketNumber,
            SourcePortal sourcePortal,
            String category,
            String subCategory,
            TicketStatus status,
            TicketPriority priority,
            Long teamId,
            Long ownerId,
            Long assigneeId,
            Boolean slaBreached,
            Boolean aiResolved,
            String search,
            Instant createdAfter,
            Instant createdBefore
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (ticketNumber != null && !ticketNumber.isBlank()) {
                predicates.add(cb.equal(root.get("ticketNumber"), ticketNumber));
            }

            if (sourcePortal != null) {
                predicates.add(cb.equal(root.get("sourcePortal"), sourcePortal));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }

            if (subCategory != null && !subCategory.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("subCategory")), subCategory.toLowerCase()));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            if (teamId != null) {
                predicates.add(cb.equal(root.get("assignedTeam").get("id"), teamId));
            }

            if (ownerId != null) {
                predicates.add(cb.equal(root.get("ticketOwner").get("id"), ownerId));
            }

            if (assigneeId != null) {
                predicates.add(cb.equal(root.get("currentAssignee").get("id"), assigneeId));
            }

            if (slaBreached != null) {
                predicates.add(cb.equal(root.get("slaBreached"), slaBreached));
            }

            if (aiResolved != null) {
                predicates.add(cb.equal(root.get("aiResolved"), aiResolved));
            }

            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("subject")), searchPattern),
                        cb.like(cb.lower(root.get("description")), searchPattern),
                        cb.like(cb.lower(root.get("customerEmail")), searchPattern),
                        cb.like(cb.lower(root.get("customerName")), searchPattern),
                        cb.like(cb.lower(root.get("ticketNumber")), searchPattern)
                );
                predicates.add(searchPredicate);
            }

            if (createdAfter != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdAfter));
            }

            if (createdBefore != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdBefore));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
