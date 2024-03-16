package com.asrevo.cvhome.commons.utils;

import lombok.Getter;

public class OperationExecution extends RuntimeException {
    @Getter
    private final ErrorCode errorCode;

    public OperationExecution(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public static OperationExecution of(ErrorCode errorCode) {
        return new OperationExecution(errorCode);
    }
}
