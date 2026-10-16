package com.filmytell.support.exception;

import com.filmytell.support.enums.TicketStatus;
import lombok.Getter;

@Getter
public class InvalidStatusTransitionException extends RuntimeException {

    private final ErrorCode errorCode;
    private final TicketStatus currentStatus;
    private final TicketStatus targetStatus;

    public InvalidStatusTransitionException(TicketStatus currentStatus, TicketStatus targetStatus) {
        super(String.format("Invalid status transition from '%s' to '%s'", currentStatus, targetStatus));
        this.errorCode = ErrorCode.INVALID_STATUS_TRANSITION;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }
}
