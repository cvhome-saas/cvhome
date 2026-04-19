package com.asrevo.cvhome.store.controller.exception;

import java.io.Serial;

public class RestApiException extends GenericRuntimeException {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    public RestApiException(String message) {
        super(message);
    }

    public RestApiException(Throwable exception) {
        super(exception);
    }

}
