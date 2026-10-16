package com.filmytell.support.dto.ticket;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTicketRequest {

    private String subject;
    private String description;
    private String category;
    private String subCategory;
}
