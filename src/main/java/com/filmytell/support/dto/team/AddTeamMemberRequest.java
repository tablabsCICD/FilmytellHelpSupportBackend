package com.filmytell.support.dto.team;

import javax.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddTeamMemberRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @Builder.Default
    private String roleInTeam = "MEMBER";
}
