package com.filmytell.support.dto.auth;

import lombok.*;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private boolean active;
    private Set<String> roles;
    private Set<String> permissions;
    private Set<String> teams;
    private Instant createdAt;
}
