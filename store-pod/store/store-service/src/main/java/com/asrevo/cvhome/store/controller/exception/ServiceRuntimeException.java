package com.asrevo.cvhome.store.controller.exception;

import org.apache.commons.lang3.StringUtils;

import java.io.Serial;

public class ServiceRuntimeException extends GenericRuntimeException {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    public ServiceRuntimeException(String errorCode, String message) {
        super(errorCode, message);
    }

    public ServiceRuntimeException(String message) {
        super(message);
    }

    public ServiceRuntimeException(Throwable exception) {
        super(exception);
    }

    public ServiceRuntimeException(String message, Throwable exception) {
        super(message, exception);
    }

    public ServiceRuntimeException(String errorCode, String message, Throwable exception) {
        super(StringUtils.isBlank(errorCode) ? "500" : errorCode, message, exception);
    }


}
