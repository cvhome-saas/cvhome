package com.asrevo.cvhome.content.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes owned by the content (CMS) context.
 *
 * <p>
 * The facade this replaces raised four different legacy runtime types and then caught {@code Exception} around them,
 * so a duplicate page code, a missing page and a database failure all left the same method as one
 * {@code LEGACY.BAD_REQUEST}. The duplicate check in particular was unreachable as a distinct outcome, which is why
 * seller-ui could never say "that code is taken".
 * </p>
 */
public enum ContentErrors implements ErrorCode {

    /**
     * No content page or box exists for that code, id or SEO url in this store.
     */
    CONTENT_NOT_FOUND("CONTENT.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * A content page or box already uses that code in this store.
     */
    DUPLICATE_CODE("CONTENT.CODE.DUPLICATE", ErrorCategory.CONFLICT),

    /**
     * The named file does not exist in this store's CMS folder.
     */
    FILE_NOT_FOUND("CONTENT.FILE.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * An uploaded file's bytes could not be read off the request.
     */
    FILE_UNREADABLE("CONTENT.FILE.UNREADABLE", ErrorCategory.STORAGE),

    /**
     * The requested folder path is not a valid directory path.
     */
    FOLDER_PATH_INVALID("CONTENT.FOLDER.PATH_INVALID", ErrorCategory.VALIDATION);

    private final String code;

    private final ErrorCategory category;

    ContentErrors(String code, ErrorCategory category) {
        this.code = code;
        this.category = category;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public ErrorCategory category() {
        return category;
    }

}
