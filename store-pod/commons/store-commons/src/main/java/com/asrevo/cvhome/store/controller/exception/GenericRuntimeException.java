package com.asrevo.cvhome.store.controller.exception;

import java.io.Serial;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GenericRuntimeException extends RuntimeException {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private final String errorCode;

    private final String errorMessage;

    public GenericRuntimeException(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public GenericRuntimeException(String errorMessage) {
        this.errorCode = null;
        this.errorMessage = errorMessage;
    }

    public GenericRuntimeException(Throwable exception) {
        super(exception);
        this.errorCode = null;
        this.errorMessage = null;
    }

    public GenericRuntimeException(String errorMessage, Throwable exception) {
        super(exception);
        this.errorCode = null;
        this.errorMessage = errorMessage;
    }

    public GenericRuntimeException(String errorCode, String errorMessage, Throwable exception) {
        super(exception);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

}
