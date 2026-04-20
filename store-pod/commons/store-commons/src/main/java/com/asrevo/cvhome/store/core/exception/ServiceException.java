package com.asrevo.cvhome.store.core.exception;

import java.io.Serial;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Exception générée par les services de l'application.
 * </p>
 */
@Setter
@Getter
public class ServiceException extends Exception {

    public static final int EXCEPTION_ERROR = 500;

    public static final int EXCEPTION_INVENTORY_MISMATCH = 120;

    @Serial
    private static final long serialVersionUID = -6854945379036729034L;

    private final int exceptionType; // regular error

    private final String messageCode;

    public ServiceException() {
        super();
        this.exceptionType = 0;
        this.messageCode = null;
    }

    public ServiceException(String messageCode) {
        super();
        this.exceptionType = 0;
        this.messageCode = messageCode;
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.exceptionType = 0;
        this.messageCode = null;
    }

    public ServiceException(Throwable cause) {
        super(cause.getMessage(), cause);
        this.exceptionType = 0;
        this.messageCode = null;
    }

    public ServiceException(int exceptionType) {
        super();
        this.exceptionType = exceptionType;
        this.messageCode = null;
    }

    public ServiceException(int exceptionType, String message) {
        super(message);
        this.exceptionType = exceptionType;
        this.messageCode = null;
    }

    public ServiceException(int exceptionType, String message, String messageCode) {
        super(message);
        this.exceptionType = exceptionType;
        this.messageCode = messageCode;
    }

}
