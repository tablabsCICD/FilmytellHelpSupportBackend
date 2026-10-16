package com.filmytell.support.dto.ticket;

import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TicketPriority;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTicketRequest {

    @NotNull(message = "Source portal is required")
    private SourcePortal sourcePortal;

    private String externalTicketId;

    private String customerId;

    private String customerName;

    private String customerEmail;

    @NotBlank(message = "Category is required")
    private String category;

    private String subCategory;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Description is required")
    private String description;

    @Builder.Default
    private TicketPriority priority = TicketPriority.MEDIUM;

    private Long assignedTeamId;

    private Long assigneeId;

    private List<String> attachmentUrls;
}
