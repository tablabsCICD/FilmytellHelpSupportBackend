package com.filmytell.support.dto.dashboard;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberWorkloadDto {

    private Long userId;
    private String username;
    private String fullName;
    private String roleInTeam;
    private long activeTasksCount;
    private long ownedTicketsCount;
}
