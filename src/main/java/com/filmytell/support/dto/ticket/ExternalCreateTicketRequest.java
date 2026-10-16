package com.filmytell.support.dto.ticket;

import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.enums.TicketPriority;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalCreateTicketRequest {

    @NotNull(message = "Source portal is required")
    private SourcePortal sourcePortal;

    @NotBlank(message = "External ticket ID is required for idempotency")
    private String externalTicketId;

    private String customerId;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Valid customer email is required")
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

    private List<String> attachmentUrls;
}
