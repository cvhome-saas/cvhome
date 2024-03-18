package com.asrevo.cvhome.store.controller;

import com.asrevo.cvhome.commons.utils.OperationExecution;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@ControllerAdvice
public class ExceptionHandlerAdvice {
    @ExceptionHandler(OperationExecution.class)
  public   ProblemDetail handleNotFoundException(OperationExecution e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        detail.setTitle("Bad Request please verify your request");
        detail.setProperty("code", e.getErrorCode().code());
        return detail;
    }
}
