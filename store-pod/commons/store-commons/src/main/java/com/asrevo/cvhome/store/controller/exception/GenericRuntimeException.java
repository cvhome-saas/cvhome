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

    private String errorCode;

    private String errorMessage;

    public GenericRuntimeException(String errorCode, String errorMessage) {
        this.setErrorCode(errorCode);
        this.setErrorMessage(errorMessage);
    }

    public GenericRuntimeException(String errorMessage) {
        this.setErrorMessage(errorMessage);
    }

    public GenericRuntimeException(Throwable exception) {
        super(exception);
        this.setErrorCode(null);
        this.setErrorMessage(null);
    }

    public GenericRuntimeException(String errorMessage, Throwable exception) {
        super(exception);
        this.setErrorCode(null);
        this.setErrorMessage(errorMessage);
    }

    public GenericRuntimeException(String errorCode, String errorMessage, Throwable exception) {
        super(exception);
        this.setErrorCode(errorCode);
        this.setErrorMessage(errorMessage);
    }

}
