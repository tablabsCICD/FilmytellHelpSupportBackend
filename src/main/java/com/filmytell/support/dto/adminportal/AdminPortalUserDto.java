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
public class AdminPortalUserDto {
    private Long id;

    @JsonAlias({"firstName", "first_name"})
    private String firstName;

    @JsonAlias({"lastName", "last_name"})
    private String lastName;

    @JsonAlias({"email", "emailId", "email_id"})
    private String email;

    @JsonAlias({"mobileNumber", "mobile_number", "mobile", "phone"})
    private String mobileNumber;

    private String username;
}
