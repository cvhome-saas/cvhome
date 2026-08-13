package com.asrevo.cvhome.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

public class InvalidMenuTreeException extends OperationNotAllowedException {
    @Serial
    private static final long serialVersionUID = 1L;

    protected InvalidMenuTreeException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static InvalidMenuTreeException because(String reason) {
        return new ErrorBuilder<>(ContentErrors.MENU_TREE_INVALID, InvalidMenuTreeException::new)
                .detail("Menu tree is invalid.")
                .param("reason", reason)
                .build();
    }
}
