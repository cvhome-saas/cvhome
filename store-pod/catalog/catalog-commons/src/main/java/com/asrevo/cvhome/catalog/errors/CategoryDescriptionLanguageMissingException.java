package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A category description was submitted with no language on it, so it cannot be stored against one.
 */
public class CategoryDescriptionLanguageMissingException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CategoryDescriptionLanguageMissingException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static CategoryDescriptionLanguageMissingException of() {
        return new ErrorBuilder<>(CatalogErrors.CATEGORY_DESCRIPTION_NO_LANGUAGE,
                        CategoryDescriptionLanguageMissingException::new)
                .detail("A category description was submitted with no language.")
                .build();
    }

}
