package com.asrevo.cvhome.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No content item, FAQ group, media asset or folder matches the identifier in this store. Content that exists but
 * belongs to another store is reported as missing, not as forbidden, so a caller cannot probe another seller's data.
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
                .param("code", code).param(STORE, store).build();
    }

    public static ContentNotFoundException byId(Long id, Object store) {
        return new ErrorBuilder<>(ContentErrors.CONTENT_NOT_FOUND, ContentNotFoundException::new)
                .detail("No content with id %s in store %s.", id, store)
                .param("id", id).param(STORE, store).build();
    }

    public static ContentNotFoundException byName(String name, Object store) {
        return new ErrorBuilder<>(ContentErrors.CONTENT_NOT_FOUND, ContentNotFoundException::new)
                .detail("No content page named %s in store %s.", name, store)
                .param("name", name).param(STORE, store).build();
    }

    public static ContentNotFoundException faqGroup(Long id, Object store) {
        return new ErrorBuilder<>(ContentErrors.FAQ_GROUP_NOT_FOUND, ContentNotFoundException::new)
                .detail("No FAQ group with id %s in store %s.", id, store)
                .param("id", id).param(STORE, store).build();
    }

    public static ContentNotFoundException media(Long id, Object store) {
        return new ErrorBuilder<>(ContentErrors.MEDIA_NOT_FOUND, ContentNotFoundException::new)
                .detail("No media asset with id %s in store %s.", id, store)
                .param("id", id).param(STORE, store).build();
    }

    public static ContentNotFoundException mediaFolder(Long id, Object store) {
        return new ErrorBuilder<>(ContentErrors.MEDIA_NOT_FOUND, ContentNotFoundException::new)
                .detail("No media folder with id %s in store %s.", id, store)
                .param("id", id).param(STORE, store).build();
    }

}
