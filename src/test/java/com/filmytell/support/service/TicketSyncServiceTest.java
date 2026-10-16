package com.filmytell.support.service;

import com.filmytell.support.dto.adminportal.AdminPortalTicketDto;
import com.filmytell.support.dto.adminportal.AdminPortalUserDto;
import com.filmytell.support.dto.ticket.TicketSyncSummaryDto;
import com.filmytell.support.entity.Ticket;
import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.repository.TicketRepository;
import com.filmytell.support.service.ai.AiSupportService;
import com.filmytell.support.service.integration.AdminPortalTicketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketSyncServiceTest {

    @Mock
    private AdminPortalTicketClient adminPortalTicketClient;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private SlaService slaService;

    @Mock
    private AuditService auditService;

    @Mock
    private AiSupportService aiSupportService;

    @InjectMocks
    private TicketSyncService ticketSyncService;

    private AdminPortalTicketDto ticketDto1;
    private AdminPortalTicketDto ticketDto2;

    @BeforeEach
    void setUp() {
        ticketDto1 = AdminPortalTicketDto.builder()
                .ticketId(101L)
                .topic("Payment failed on subscription")
                .email("user1@example.com")
                .mobileNumber("9876543210")
                .user(AdminPortalUserDto.builder().firstName("John").lastName("Doe").build())
                .build();

        ticketDto2 = AdminPortalTicketDto.builder()
                .ticketId(102L)
                .topic("Video buffering issue")
                .email("user2@example.com")
                .mobileNumber("9876543211")
                .user(AdminPortalUserDto.builder().firstName("Jane").lastName("Smith").build())
                .build();
    }

    @Test
    void syncTicketsFromAdminPortal_NewTickets_ShouldInsertAll() {
        when(adminPortalTicketClient.fetchAllTickets()).thenReturn(List.of(ticketDto1, ticketDto2));
        when(ticketRepository.existsBySourceSystemAndSourceTicketId(anyString(), anyString())).thenReturn(false);
        when(ticketRepository.existsBySourcePortalAndExternalTicketId(any(), anyString())).thenReturn(false);
        when(ticketRepository.saveAndFlush(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketSyncSummaryDto summary = ticketSyncService.syncTicketsFromAdminPortal();

        assertTrue(summary.isSuccess());
        assertEquals(2, summary.getTotalFetched());
        assertEquals(2, summary.getNewTickets());
        assertEquals(0, summary.getExistingTickets());
        assertEquals(0, summary.getDuplicatesSkipped());
        verify(ticketRepository, times(2)).saveAndFlush(any(Ticket.class));
    }

    @Test
    void syncTicketsFromAdminPortal_RepeatedSync_ShouldSkipDuplicates() {
        when(adminPortalTicketClient.fetchAllTickets()).thenReturn(List.of(ticketDto1));
        when(ticketRepository.existsBySourceSystemAndSourceTicketId("ADMIN_PORTAL", "101")).thenReturn(true);

        TicketSyncSummaryDto summary = ticketSyncService.syncTicketsFromAdminPortal();

        assertTrue(summary.isSuccess());
        assertEquals(1, summary.getTotalFetched());
        assertEquals(0, summary.getNewTickets());
        assertEquals(1, summary.getExistingTickets());
        assertEquals(1, summary.getDuplicatesSkipped());
        verify(ticketRepository, never()).saveAndFlush(any(Ticket.class));
    }

    @Test
    void syncTicketsFromAdminPortal_ApiFailure_ShouldHandleGracefully() {
        when(adminPortalTicketClient.fetchAllTickets()).thenReturn(List.of());

        TicketSyncSummaryDto summary = ticketSyncService.syncTicketsFromAdminPortal();

        assertTrue(summary.isSuccess());
        assertEquals(0, summary.getTotalFetched());
        assertEquals(0, summary.getNewTickets());
        assertEquals(0, summary.getDuplicatesSkipped());
        verify(ticketRepository, never()).saveAndFlush(any(Ticket.class));
    }
}
