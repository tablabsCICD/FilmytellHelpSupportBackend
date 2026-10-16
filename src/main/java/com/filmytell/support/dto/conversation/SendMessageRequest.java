package com.filmytell.support.dto.conversation;

import com.filmytell.support.enums.MessageType;
import com.filmytell.support.enums.SenderType;
import javax.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendMessageRequest {

    @NotBlank(message = "Message text is required")
    private String message;

    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    private SenderType senderType;

    private String senderName;

    @Builder.Default
    private boolean isInternal = false;
}
