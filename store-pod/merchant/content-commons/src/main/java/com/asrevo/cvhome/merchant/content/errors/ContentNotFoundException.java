package com.asrevo.cvhome.merchant.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No content page or box matches the identifier in this store.
 *
 * <p>
 * Covers the cross-store case too: content that exists but belongs to another store is reported as missing, not as
 * forbidden, so a caller cannot probe for the existence of another seller's pages.
 * </p>
 */
public class ContentNotFoundException extends ResourceNotFoundException {

    private static final String STORE = "store";

    @Serial
    private static final long serialVersionUID = 1L;

    protected ContentNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ContentNotFoundException byCode(String code, Object store) {
        return new ErrorBuilder<>(ContentErrors.CONTENT_NOT_FOUND, ContentNotFoundException::new)
                .detail("No content with code %s in store %s.", code, store)
                .param("code", code)
                .param(STORE, store)
                .build();
    }

    public static ContentNotFoundException byId(Long id, Object store) {
        return new ErrorBuilder<>(ContentErrors.CONTENT_NOT_FOUND, ContentNotFoundException::new)
                .detail("No content with id %s in store %s.", id, store)
                .param("id", id)
                .param(STORE, store)
                .build();
    }

    public static ContentNotFoundException byName(String name, Object store) {
        return new ErrorBuilder<>(ContentErrors.CONTENT_NOT_FOUND, ContentNotFoundException::new)
                .detail("No content page named %s in store %s.", name, store)
                .param("name", name)
                .param(STORE, store)
                .build();
    }

}
