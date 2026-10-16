package com.filmytell.support.exception;

import lombok.Getter;

@Getter
public class DelegationException extends RuntimeException {

    private final ErrorCode errorCode;

    public DelegationException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public DelegationException(String message) {
        super(message);
        this.errorCode = ErrorCode.TICKET_DELEGATION_NOT_ALLOWED;
    }
}
