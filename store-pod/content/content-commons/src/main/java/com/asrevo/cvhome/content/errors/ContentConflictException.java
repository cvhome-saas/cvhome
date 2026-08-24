package com.asrevo.cvhome.content.errors;

import java.io.Serial;
import java.util.List;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * The write conflicts with current state: a slug already taken, a stale version, or a row something else still
 * points at. Renders as HTTP 409.
 */
public class ContentConflictException extends DuplicateResourceException {

    private static final String STORE = "store";

    private static final String ID = "id";

    @Serial
    private static final long serialVersionUID = 1L;

    protected ContentConflictException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ContentConflictException slugDuplicate(String type, String slug, Object store) {
        return new ErrorBuilder<>(ContentErrors.SLUG_DUPLICATE, ContentConflictException::new)
                .detail("%s slug %s already exists in store %s.", type, slug, store)
                .param("contentType", type).param("slug", slug).param(STORE, store).build();
    }

    public static ContentConflictException versionConflict(Long id, Integer sent, Integer current) {
        return new ErrorBuilder<>(ContentErrors.VERSION_CONFLICT, ContentConflictException::new)
                .detail("Content %s was changed by someone else (you sent version %s, current is %s).", id, sent,
                        current)
                .param(ID, id).param("sentVersion", sent).param("currentVersion", current).build();
    }

    public static ContentConflictException pageReferenced(Long id, List<String> menus) {
        return new ErrorBuilder<>(ContentErrors.PAGE_REFERENCED, ContentConflictException::new)
                .detail("Page %s is linked from menus %s; pass force=true to delete anyway.", id, menus)
                .param(ID, id).param("menus", menus).build();
    }

    public static ContentConflictException policyTypeActive(String type, Long existingId, Object store) {
        return new ErrorBuilder<>(ContentErrors.POLICY_TYPE_ACTIVE_EXISTS, ContentConflictException::new)
                .detail("Store %s already has a %s policy (id %s).", store, type, existingId)
                .param("policyType", type).param("existingId", existingId).param(STORE, store).build();
    }

    public static ContentConflictException mediaReferenced(Long id, List<?> usage) {
        return new ErrorBuilder<>(ContentErrors.MEDIA_REFERENCED, ContentConflictException::new)
                .detail("Media asset %s is still used by %s item(s); pass force=true to delete anyway.", id,
                        usage.size())
                .param(ID, id).param("usage", usage).build();
    }

    public static ContentConflictException folderNotEmpty(Long id, long count) {
        return new ErrorBuilder<>(ContentErrors.MEDIA_FOLDER_NOT_EMPTY, ContentConflictException::new)
                .detail("Folder %s still holds %s file(s); pass moveTo=<folderId> to move them first.", id, count)
                .param(ID, id).param("fileCount", count).build();
    }

}
