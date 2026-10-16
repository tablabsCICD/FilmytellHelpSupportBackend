package com.filmytell.support.dto.adminportal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminPortalTicketDto {
    @JsonAlias({"tickedId", "ticketId", "ticket_id", "id", "_id"})
    private Object ticketId;

    @JsonAlias({"topic", "subject", "title"})
    private String topic;

    @JsonAlias({"mobileNumber", "mobile_number", "mobile", "phone"})
    private String mobileNumber;

    @JsonAlias({"email", "emailId", "email_id"})
    private String email;

    @JsonAlias({"description", "details", "message"})
    private String description;

    @JsonAlias({"status", "ticketStatus"})
    private String status;

    @JsonAlias({"priority", "ticketPriority"})
    private String priority;

    @JsonAlias({"category", "subCategory", "type"})
    private String category;

    @JsonAlias({"createdAt", "created_at", "date"})
    private String createdAt;

    private Boolean isResolved;
    private AdminPortalUserDto user;

    public String getSourceTicketId() {
        return ticketId != null ? ticketId.toString() : null;
    }
}
