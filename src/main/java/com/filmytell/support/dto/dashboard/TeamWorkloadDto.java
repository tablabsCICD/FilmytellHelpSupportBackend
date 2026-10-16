package com.filmytell.support.dto.dashboard;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamWorkloadDto {

    private Long teamId;
    private String teamName;
    private String teamCode;
    private long totalAssignedTickets;
    private long openTickets;
    private long urgentTickets;
    private List<MemberWorkloadDto> memberWorkloads;
}
