package com.filmytell.support.repository;

import com.filmytell.support.entity.TicketTask;
import com.filmytell.support.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketTaskRepository extends JpaRepository<TicketTask, Long> {

    List<TicketTask> findByTicketIdOrderByDelegationLevelAscCreatedAtAsc(Long ticketId);

    List<TicketTask> findByTicketIdAndStatus(Long ticketId, TaskStatus status);

    Optional<TicketTask> findByTicketIdAndParentTaskIsNull(Long ticketId);

    List<TicketTask> findByParentTaskId(Long parentTaskId);

    List<TicketTask> findByAssignedToUserIdAndStatus(Long userId, TaskStatus status);

    @Query("""
        SELECT task FROM TicketTask task
        WHERE task.ticket.id = :ticketId
          AND task.status IN ('PENDING', 'IN_PROGRESS')
        ORDER BY task.delegationLevel DESC
    """)
    List<TicketTask> findActiveTasksByTicketId(Long ticketId);

    @Query("SELECT COUNT(task) FROM TicketTask task WHERE task.delegationLevel > 0")
    long countTotalDelegatedTasks();

    @Query("SELECT AVG(task.delegationLevel) FROM TicketTask task WHERE task.delegationLevel > 0")
    Double findAverageDelegationDepth();
}
