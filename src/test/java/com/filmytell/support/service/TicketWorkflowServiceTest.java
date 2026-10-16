package com.filmytell.support.service;

import com.filmytell.support.dto.ticket.AssignTicketRequest;
import com.filmytell.support.dto.ticket.TicketResponse;
import com.filmytell.support.dto.ticket.UpdatePriorityRequest;
import com.filmytell.support.dto.ticket.UpdateStatusRequest;
import com.filmytell.support.entity.Ticket;
import com.filmytell.support.entity.User;
import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TicketPriority;
import com.filmytell.support.enums.TicketStatus;
import com.filmytell.support.exception.InvalidStatusTransitionException;
import com.filmytell.support.mapper.ConversationMapper;
import com.filmytell.support.mapper.TaskMapper;
import com.filmytell.support.mapper.TicketMapper;
import com.filmytell.support.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketWorkflowServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TicketTaskRepository taskRepository;

    @Mock
    private TicketAssignmentRepository assignmentRepository;

    @Mock
    private TicketPriorityHistoryRepository priorityHistoryRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private NotificationService notificationService;

    @Spy
    private TicketMapper ticketMapper = new TicketMapper(new TaskMapper(), new ConversationMapper());

    @InjectMocks
    private TicketWorkflowService workflowService;

    private Ticket ticket;
    private User agentA;

    @BeforeEach
    void setUp() {
        agentA = User.builder().id(1L).username("agent_a").firstName("Agent").lastName("A").active(true).build();

        ticket = Ticket.builder()
                .id(100L)
                .ticketNumber("FLM-1001")
                .sourcePortal(SourcePortal.FILMYTELL)
                .category("ACCOUNT_LOGIN")
                .subject("Password Reset Issue")
                .description("Cannot receive OTP on email")
                .status(TicketStatus.NEW)
                .priority(TicketPriority.MEDIUM)
                .tasks(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Ticket Assignment: Sets Ticket Owner, Assignee, and Status to ASSIGNED")
    void testTicketAssignment() {
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(1L)).thenReturn(Optional.of(agentA));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        AssignTicketRequest request = AssignTicketRequest.builder()
                .assigneeId(1L)
                .reason("Primary support assignment")
                .build();

        TicketResponse response = workflowService.assignTicket(100L, request);

        assertNotNull(response);
        assertEquals(TicketStatus.ASSIGNED, response.getStatus());
        assertEquals(agentA.getId(), response.getTicketOwnerId());
        assertEquals(agentA.getId(), response.getCurrentAssigneeId());
    }

    @Test
    @DisplayName("Status Transition: Valid IN_PROGRESS -> RESOLVED")
    void testValidStatusTransition() {
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        UpdateStatusRequest request = UpdateStatusRequest.builder()
                .status(TicketStatus.RESOLVED)
                .reason("Customer issue resolved")
                .build();

        TicketResponse response = workflowService.updateStatus(100L, request);

        assertEquals(TicketStatus.RESOLVED, response.getStatus());
        assertNotNull(ticket.getResolvedAt());
    }

    @Test
    @DisplayName("Status Transition: Invalid CLOSED -> IN_PROGRESS Disallowed")
    void testInvalidStatusTransition_Disallowed() {
        ticket.setStatus(TicketStatus.CLOSED);
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));

        UpdateStatusRequest request = UpdateStatusRequest.builder()
                .status(TicketStatus.IN_PROGRESS)
                .build();

        assertThrows(InvalidStatusTransitionException.class, () ->
                workflowService.updateStatus(100L, request)
        );
    }

    @Test
    @DisplayName("Priority Update: Records History & Updates Priority")
    void testPriorityUpdate() {
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        UpdatePriorityRequest request = UpdatePriorityRequest.builder()
                .priority(TicketPriority.URGENT)
                .reason("VIP Customer escalation")
                .build();

        TicketResponse response = workflowService.updatePriority(100L, request);

        assertEquals(TicketPriority.URGENT, response.getPriority());
        verify(priorityHistoryRepository, times(1)).save(any());
    }
}
