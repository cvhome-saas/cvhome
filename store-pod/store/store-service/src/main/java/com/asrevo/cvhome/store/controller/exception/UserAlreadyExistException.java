package com.asrevo.cvhome.store.controller.exception;

import java.io.Serial;

public class UserAlreadyExistException extends Exception {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    public UserAlreadyExistException(String message) {
        super(message, null);
    }

}
