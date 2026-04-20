package com.asrevo.cvhome.merchant.controller.exception;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.asrevo.cvhome.store.controller.exception.ErrorEntity;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class FileUploadExceptionAdvice {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
    public @ResponseBody ErrorEntity handleFileException(Exception exception) {
        log.error(exception.getMessage(), exception);
        ErrorEntity errorEntity = new ErrorEntity();

        String resultMessage = exception.getLocalizedMessage() != null ? exception.getLocalizedMessage()
                : exception.getMessage();
        Optional.ofNullable(resultMessage).ifPresent(errorEntity::setMessage);
        return errorEntity;
    }

}
