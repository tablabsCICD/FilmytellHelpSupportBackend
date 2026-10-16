package com.filmytell.support.dto.team;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberDto {

    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String roleInTeam;
    private boolean active;
    private Instant joinedAt;
}
