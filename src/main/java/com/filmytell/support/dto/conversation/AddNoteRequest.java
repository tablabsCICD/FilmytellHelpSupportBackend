package com.filmytell.support.dto.conversation;

import javax.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddNoteRequest {

    @NotBlank(message = "Note content cannot be empty")
    private String note;
}
