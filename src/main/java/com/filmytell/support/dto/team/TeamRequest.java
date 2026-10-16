package com.filmytell.support.dto.team;

import javax.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRequest {

    @NotBlank(message = "Team name is required")
    private String name;

    @NotBlank(message = "Team code is required")
    private String code;

    private String description;
}
