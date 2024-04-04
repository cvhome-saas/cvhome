package com.asrevo.cvhome.store.controller.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ErrorEntity {
    private String errorCode;
    private String message;

}
