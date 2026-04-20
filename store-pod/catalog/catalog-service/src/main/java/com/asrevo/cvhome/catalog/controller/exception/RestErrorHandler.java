package com.asrevo.cvhome.catalog.controller.exception;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.controller.exception.ErrorEntity;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.RestApiException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.controller.exception.UnauthorizedException;

import lombok.extern.slf4j.Slf4j;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice({"com.asrevo.cvhome.store.controller"})
@Slf4j
public class RestErrorHandler {

    @RequestMapping(produces = "application/json")
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorEntity handleServiceException(Exception exception) {
        log.error(exception.getMessage(), exception);
        Objects.requireNonNull(exception.getCause());
        Throwable rootCause = exception.getCause();
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return createErrorEntity(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), exception.getMessage(),
                rootCause.getMessage());
    }

    /**
     * Generic exception serviceException handler
     */
    @RequestMapping(produces = "application/json")
    @ExceptionHandler(ServiceRuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorEntity handleServiceException(ServiceRuntimeException exception) {
        log.error(exception.getErrorMessage(), exception);
        Throwable rootCause = exception.getCause();
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return createErrorEntity(
                exception.getErrorCode() != null ? exception.getErrorCode()
                        : String.valueOf(HttpStatus.BAD_REQUEST.value()),
                exception.getErrorMessage(), rootCause.getMessage());
    }

    @RequestMapping(produces = "application/json")
    @ExceptionHandler(ConversionRuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorEntity handleServiceException(ConversionRuntimeException exception) {
        log.error(exception.getErrorMessage(), exception);
        return createErrorEntity(exception.getErrorCode(), exception.getErrorMessage(),
                exception.getLocalizedMessage());
    }

    @RequestMapping(produces = "application/json")
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ErrorEntity handleServiceException(ResourceNotFoundException exception) {
        log.error(exception.getErrorMessage(), exception);

        return createErrorEntity(exception.getErrorCode(), exception.getErrorMessage(),
                exception.getLocalizedMessage());
    }

    @RequestMapping(produces = "application/json")
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public @ResponseBody ErrorEntity handleServiceException(UnauthorizedException exception) {
        log.error(exception.getErrorMessage(), exception);

        return createErrorEntity(exception.getErrorCode(), exception.getErrorMessage(),
                exception.getLocalizedMessage());
    }

    @RequestMapping(produces = "application/json")
    @ExceptionHandler(RestApiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorEntity handleRestApiException(RestApiException exception) {
        log.error(exception.getErrorMessage(), exception);

        return createErrorEntity(exception.getErrorCode(), exception.getErrorMessage(),
                exception.getLocalizedMessage());
    }

    private ErrorEntity createErrorEntity(String errorCode, String message, String detailMessage) {
        ErrorEntity errorEntity = new ErrorEntity();
        Optional.ofNullable(errorCode).ifPresent(errorEntity::setErrorCode);

        String resultMessage = (message != null && detailMessage != null)
                ? new StringBuilder().append(message).append(", ").append(detailMessage).toString() : detailMessage;
        if (StringUtils.isBlank(resultMessage)) {
            resultMessage = message;
        }
        Optional.ofNullable(resultMessage).ifPresent(errorEntity::setMessage);
        return errorEntity;
    }

}
