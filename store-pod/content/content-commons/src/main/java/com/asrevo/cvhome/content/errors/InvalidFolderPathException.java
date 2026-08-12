package com.asrevo.cvhome.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * The requested folder path is not a valid directory path.
 *
 * <p>
 * A 400: the path came straight from the request, and the check exists to keep a crafted path from escaping the
 * store's own folder. It was a {@code ServiceException} and so rendered as a 500, which reads as "the platform is
 * broken" for what is simply a rejected input.
 * </p>
 */
public class InvalidFolderPathException extends ValidationException {

    private static final String PATH = "path";

    @Serial
    private static final long serialVersionUID = 1L;

    protected InvalidFolderPathException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static InvalidFolderPathException of(String path) {
        return new ErrorBuilder<>(ContentErrors.FOLDER_PATH_INVALID, InvalidFolderPathException::new)
                .detail("%s is not a valid directory path.", path)
                .param(PATH, path)
                .fieldError(PATH, ContentErrors.FOLDER_PATH_INVALID, "Not a valid directory path.")
                .build();
    }

}
