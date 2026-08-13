package com.asrevo.cvhome.content.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

public enum ContentErrors implements ErrorCode {
    CONTENT_NOT_FOUND("CONTENT.CONTENT.NOT_FOUND", ErrorCategory.NOT_FOUND),
    CONTENT_TRANSITION_ILLEGAL("CONTENT.CONTENT.TRANSITION_ILLEGAL", ErrorCategory.UNPROCESSABLE),
    CONTENT_VERSION_CONFLICT("CONTENT.CONTENT.VERSION_CONFLICT", ErrorCategory.CONFLICT),
    CONTENT_DEFAULT_TRANSLATION_REQUIRED("CONTENT.TRANSLATION.DEFAULT_REQUIRED", ErrorCategory.UNPROCESSABLE),
    MEDIA_INVALID("CONTENT.MEDIA.INVALID", ErrorCategory.VALIDATION),
    MEDIA_STORAGE_FAILED("CONTENT.MEDIA.STORAGE_FAILED", ErrorCategory.STORAGE),
    BANNER_CAPACITY_EXCEEDED("CONTENT.BANNER.CAPACITY_EXCEEDED", ErrorCategory.UNPROCESSABLE),
    BANNER_ARTWORK_REQUIRED("CONTENT.BANNER.ARTWORK_REQUIRED", ErrorCategory.UNPROCESSABLE),
    FAQ_GROUP_NOT_FOUND("CONTENT.FAQ.GROUP_NOT_FOUND", ErrorCategory.NOT_FOUND),
    FAQ_REORDER_INVALID("CONTENT.FAQ.REORDER_INVALID", ErrorCategory.UNPROCESSABLE),
    MENU_TREE_INVALID("CONTENT.MENU.TREE_INVALID", ErrorCategory.UNPROCESSABLE),
    POLICY_NOT_FOUND("CONTENT.POLICY.NOT_FOUND", ErrorCategory.NOT_FOUND),
    POLICY_IMMUTABLE("CONTENT.POLICY.IMMUTABLE", ErrorCategory.UNPROCESSABLE);

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
