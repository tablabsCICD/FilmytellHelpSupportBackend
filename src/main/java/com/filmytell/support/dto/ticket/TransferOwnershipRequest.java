package com.filmytell.support.dto.ticket;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferOwnershipRequest {

    @NotNull(message = "New owner ID is required")
    private Long newOwnerId;

    @NotBlank(message = "Reason for ownership transfer is required")
    private String reason;
}
