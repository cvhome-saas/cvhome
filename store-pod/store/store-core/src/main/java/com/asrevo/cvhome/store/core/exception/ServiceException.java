package com.asrevo.cvhome.store.core.exception;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * <p>Exception générée par les services de l'application.</p>
 */
@Setter
@Getter
public class ServiceException extends Exception {

    public final static int EXCEPTION_ERROR = 500;
    public final static int EXCEPTION_VALIDATION = 99;
    public final static int EXCEPTION_PAYMENT_DECLINED = 100;
    public final static int EXCEPTION_TRANSACTION_DECLINED = 101;
    public final static int EXCEPTION_INVENTORY_MISMATCH = 120;
    @Serial
    private static final long serialVersionUID = -6854945379036729034L;
    private int exceptionType = 0;//regular error
    private String messageCode = null;


    public ServiceException() {
        super();
    }

    public ServiceException(String messageCode) {
        super();
        this.messageCode = messageCode;
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    public ServiceException(int exceptionType) {
        super();
        this.exceptionType = exceptionType;
    }

    public ServiceException(int exceptionType, String message) {
        super(message);
        this.exceptionType = exceptionType;
    }

    public ServiceException(int exceptionType, String message, String messageCode) {
        super(message);
        this.messageCode = messageCode;
        this.exceptionType = exceptionType;
    }

}