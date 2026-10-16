package com.filmytell.support.dto.team;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponse {

    private Long id;
    private String name;
    private String code;
    private String description;
    private boolean active;
    private int memberCount;
    private List<TeamMemberDto> members;
    private Instant createdAt;
}
