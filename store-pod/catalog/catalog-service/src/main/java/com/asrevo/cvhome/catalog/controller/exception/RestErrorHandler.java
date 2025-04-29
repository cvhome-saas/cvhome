package com.asrevo.cvhome.catalog.controller.exception;

import com.asrevo.cvhome.store.controller.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

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
        return createErrorEntity("500", exception.getMessage(),
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
        return createErrorEntity(exception.getErrorCode() != null ? exception.getErrorCode() : "500", exception.getErrorMessage(),
                rootCause.getMessage());
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
        Optional.ofNullable(errorCode)
                .ifPresent(errorEntity::setErrorCode);

        String resultMessage = (message != null && detailMessage != null) ? new StringBuilder().append(message).append(", ").append(detailMessage).toString() : detailMessage;
        if (StringUtils.isBlank(resultMessage)) {
            resultMessage = message;
        }
        Optional.ofNullable(resultMessage)
                .ifPresent(errorEntity::setMessage);
        return errorEntity;
    }
}
