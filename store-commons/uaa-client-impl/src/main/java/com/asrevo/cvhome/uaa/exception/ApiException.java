package com.asrevo.cvhome.uaa.exception;


public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Exception e) {
        super(message, e);
    }
}

