package com.asrevo.cvhome.commons.utils;

import lombok.Getter;

@Getter
public class OperationExecution extends RuntimeException {

    private final transient ErrorCode errorCode;

    public OperationExecution(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public static OperationExecution of(ErrorCode errorCode) {
        return new OperationExecution(errorCode);
    }

}
