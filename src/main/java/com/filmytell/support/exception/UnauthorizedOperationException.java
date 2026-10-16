package com.filmytell.support.exception;

import lombok.Getter;

@Getter
public class UnauthorizedOperationException extends RuntimeException {

    private final ErrorCode errorCode;

    public UnauthorizedOperationException(String message) {
        super(message);
        this.errorCode = ErrorCode.UNAUTHORIZED_OPERATION;
    }
}
