package com.filmytell.support.service;

import com.filmytell.support.dto.task.CompleteTaskRequest;
import com.filmytell.support.dto.task.DelegateTaskRequest;
import com.filmytell.support.dto.task.TicketTaskResponse;
import com.filmytell.support.entity.*;
import com.filmytell.support.enums.*;
import com.filmytell.support.exception.DelegationException;
import com.filmytell.support.exception.ErrorCode;
import com.filmytell.support.mapper.TaskMapper;
import com.filmytell.support.repository.TicketRepository;
import com.filmytell.support.repository.TicketTaskRepository;
import com.filmytell.support.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskDelegationServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketTaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private NotificationService notificationService;

    @Spy
    private TaskMapper taskMapper = new TaskMapper();

    @InjectMocks
    private TaskDelegationService taskDelegationService;

    private User userA;
    private User userB;
    private User userC;
    private User userD;
    private User inactiveUser;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(taskDelegationService, "maxDelegationDepth", 5);

        userA = User.builder().id(1L).username("agent_a").firstName("Agent").lastName("A").active(true).build();
        userB = User.builder().id(2L).username("agent_b").firstName("Agent").lastName("B").active(true).build();
        userC = User.builder().id(3L).username("agent_c").firstName("Agent").lastName("C").active(true).build();
        userD = User.builder().id(4L).username("agent_d").firstName("Agent").lastName("D").active(true).build();
        inactiveUser = User.builder().id(5L).username("inactive_u").firstName("Inactive").lastName("U").active(false).build();

        ticket = Ticket.builder()
                .id(100L)
                .ticketNumber("FLM-1001")
                .sourcePortal(SourcePortal.FILMYTELL)
                .category("PLAYBACK")
                .subject("Video Buffering")
                .description("Buffering on Android TV")
                .status(TicketStatus.IN_PROGRESS)
                .priority(TicketPriority.HIGH)
                .ticketOwner(userA)
                .currentAssignee(userA)
                .tasks(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Case 1: Successful Delegation A -> B (Level 1)")
    void testDelegation_A_to_B_Success() {
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(userB));
        when(taskRepository.findActiveTasksByTicketId(100L)).thenReturn(List.of());
        when(taskRepository.save(any(TicketTask.class))).thenAnswer(invocation -> {
            TicketTask t = invocation.getArgument(0);
            t.setId(10L);
            return t;
        });

        DelegateTaskRequest request = DelegateTaskRequest.builder()
                .assignToUserId(2L)
                .reason(DelegationReason.EXPERTISE)
                .instructions("Please inspect video CDN logs")
                .build();

        TicketTaskResponse response = taskDelegationService.delegateTask(100L, request);

        assertNotNull(response);
        assertEquals(1, response.getDelegationLevel());
        assertEquals(userB.getId(), response.getAssignedToUserId());
        assertEquals(userA.getId(), response.getAssignedFromUserId());

        // Verify Ticket Owner remains A, while Current Assignee is B
        assertEquals(userA, ticket.getTicketOwner());
        assertEquals(userB, ticket.getCurrentAssignee());
        assertEquals(userA, ticket.getDelegatedFrom());
    }

    @Test
    @DisplayName("Case 2: Successful Multi-Level Delegation A -> B -> C (Level 2)")
    void testDelegation_A_to_B_to_C_Success() {
        TicketTask taskLevel1 = TicketTask.builder()
                .id(10L)
                .ticket(ticket)
                .assignedFromUser(userA)
                .assignedToUser(userB)
                .delegationLevel(1)
                .reason(DelegationReason.EXPERTISE)
                .status(TaskStatus.IN_PROGRESS)
                .build();

        ticket.setCurrentAssignee(userB);
        ticket.setDelegatedFrom(userA);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(3L)).thenReturn(Optional.of(userC));
        when(taskRepository.findActiveTasksByTicketId(100L)).thenReturn(List.of(taskLevel1));
        when(taskRepository.save(any(TicketTask.class))).thenAnswer(invocation -> {
            TicketTask t = invocation.getArgument(0);
            t.setId(20L);
            return t;
        });

        DelegateTaskRequest request = DelegateTaskRequest.builder()
                .assignToUserId(3L)
                .reason(DelegationReason.TECHNICAL_DEPENDENCY)
                .instructions("Deep packet inspection needed")
                .build();

        TicketTaskResponse response = taskDelegationService.delegateTask(100L, request);

        assertNotNull(response);
        assertEquals(2, response.getDelegationLevel());
        assertEquals(userC.getId(), response.getAssignedToUserId());
        assertEquals(userB.getId(), response.getAssignedFromUserId());

        // Ownership is still User A!
        assertEquals(userA, ticket.getTicketOwner());
        assertEquals(userC, ticket.getCurrentAssignee());
        assertEquals(userB, ticket.getDelegatedFrom());
    }

    @Test
    @DisplayName("Case 3: Successful Multi-Level Delegation A -> B -> C -> D (Level 3)")
    void testDelegation_A_to_B_to_C_to_D_Success() {
        TicketTask taskLevel1 = TicketTask.builder()
                .id(10L)
                .ticket(ticket)
                .assignedFromUser(userA)
                .assignedToUser(userB)
                .delegationLevel(1)
                .build();

        TicketTask taskLevel2 = TicketTask.builder()
                .id(20L)
                .ticket(ticket)
                .parentTask(taskLevel1)
                .assignedFromUser(userB)
                .assignedToUser(userC)
                .delegationLevel(2)
                .build();

        ticket.setCurrentAssignee(userC);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(4L)).thenReturn(Optional.of(userD));
        when(taskRepository.findActiveTasksByTicketId(100L)).thenReturn(List.of(taskLevel2, taskLevel1));
        when(taskRepository.save(any(TicketTask.class))).thenAnswer(invocation -> {
            TicketTask t = invocation.getArgument(0);
            t.setId(30L);
            return t;
        });

        DelegateTaskRequest request = DelegateTaskRequest.builder()
                .assignToUserId(4L)
                .reason(DelegationReason.TECHNICAL_DEPENDENCY)
                .instructions("Transcoder core dump investigation")
                .build();

        TicketTaskResponse response = taskDelegationService.delegateTask(100L, request);

        assertEquals(3, response.getDelegationLevel());
        assertEquals(userD.getId(), response.getAssignedToUserId());
        assertEquals(userA, ticket.getTicketOwner());
    }

    @Test
    @DisplayName("Case 4: Self-Delegation A -> A Must Fail")
    void testSelfDelegation_MustFail() {
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(1L)).thenReturn(Optional.of(userA));

        DelegateTaskRequest request = DelegateTaskRequest.builder()
                .assignToUserId(1L)
                .reason(DelegationReason.EXPERTISE)
                .instructions("Self delegating")
                .build();

        DelegationException exception = assertThrows(DelegationException.class, () ->
                taskDelegationService.delegateTask(100L, request)
        );

        assertEquals(ErrorCode.DELEGATION_SELF_NOT_ALLOWED, exception.getErrorCode());
    }

    @Test
    @DisplayName("Case 5: Circular Delegation A -> B -> A Must Fail")
    void testCircularDelegation_MustFail() {
        TicketTask taskLevel1 = TicketTask.builder()
                .id(10L)
                .ticket(ticket)
                .assignedFromUser(userA)
                .assignedToUser(userB)
                .delegationLevel(1)
                .build();

        ticket.setCurrentAssignee(userB);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(1L)).thenReturn(Optional.of(userA));
        when(taskRepository.findActiveTasksByTicketId(100L)).thenReturn(List.of(taskLevel1));

        DelegateTaskRequest request = DelegateTaskRequest.builder()
                .assignToUserId(1L) // Attempting to delegate back to ancestor A
                .reason(DelegationReason.EXPERTISE)
                .instructions("Circular loop")
                .build();

        DelegationException exception = assertThrows(DelegationException.class, () ->
                taskDelegationService.delegateTask(100L, request)
        );

        assertEquals(ErrorCode.DELEGATION_CIRCULAR_LOOP, exception.getErrorCode());
    }

    @Test
    @DisplayName("Case 6: Inactive User Delegation Must Fail")
    void testInactiveUserDelegation_MustFail() {
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(5L)).thenReturn(Optional.of(inactiveUser));

        DelegateTaskRequest request = DelegateTaskRequest.builder()
                .assignToUserId(5L)
                .reason(DelegationReason.WORKLOAD)
                .instructions("Delegate to inactive agent")
                .build();

        DelegationException exception = assertThrows(DelegationException.class, () ->
                taskDelegationService.delegateTask(100L, request)
        );

        assertEquals(ErrorCode.USER_NOT_ACTIVE, exception.getErrorCode());
    }

    @Test
    @DisplayName("Case 7: Subtask Completion Cascades Back to Parent Delegator")
    void testTaskCompletion_CascadesToParent() {
        TicketTask parentTask = TicketTask.builder()
                .id(10L)
                .ticket(ticket)
                .assignedFromUser(userA)
                .assignedToUser(userB)
                .delegationLevel(1)
                .build();

        TicketTask subTask = TicketTask.builder()
                .id(20L)
                .ticket(ticket)
                .parentTask(parentTask)
                .assignedFromUser(userB)
                .assignedToUser(userC)
                .delegationLevel(2)
                .status(TaskStatus.IN_PROGRESS)
                .build();

        when(taskRepository.findById(20L)).thenReturn(Optional.of(subTask));
        when(taskRepository.save(any(TicketTask.class))).thenAnswer(i -> i.getArgument(0));

        CompleteTaskRequest request = CompleteTaskRequest.builder()
                .completionNotes("CDN route repaired")
                .build();

        TicketTaskResponse response = taskDelegationService.completeTask(20L, request);

        assertEquals(TaskStatus.COMPLETED, response.getStatus());
        // Ticket's current assignee reverts back to B
        assertEquals(userB, ticket.getCurrentAssignee());
    }
}
