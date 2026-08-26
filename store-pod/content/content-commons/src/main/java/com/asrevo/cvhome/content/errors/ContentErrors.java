package com.asrevo.cvhome.content.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes owned by the content platform (pages, posts, banners, FAQ, policies, menus, media).
 */
public enum ContentErrors implements ErrorCode {

    CONTENT_NOT_FOUND("CONTENT.NOT_FOUND", ErrorCategory.NOT_FOUND),
    SLUG_DUPLICATE("CONTENT.SLUG.DUPLICATE", ErrorCategory.CONFLICT),
    VERSION_CONFLICT("CONTENT.VERSION.CONFLICT", ErrorCategory.CONFLICT),
    TRANSITION_NOT_ALLOWED("CONTENT.STATUS.TRANSITION_NOT_ALLOWED", ErrorCategory.UNPROCESSABLE),
    PUBLISH_INCOMPLETE("CONTENT.PUBLISH.INCOMPLETE", ErrorCategory.UNPROCESSABLE),
    SCHEDULE_INVALID("CONTENT.SCHEDULE.INVALID", ErrorCategory.VALIDATION),
    PAGE_REFERENCED("CONTENT.PAGE.REFERENCED", ErrorCategory.CONFLICT),
    BANNER_CAPACITY_EXCEEDED("CONTENT.BANNER.CAPACITY_EXCEEDED", ErrorCategory.UNPROCESSABLE),
    MENU_DEPTH_EXCEEDED("CONTENT.MENU.DEPTH_EXCEEDED", ErrorCategory.UNPROCESSABLE),
    MENU_TARGET_INVALID("CONTENT.MENU.TARGET_INVALID", ErrorCategory.VALIDATION),
    POLICY_VERSION_IMMUTABLE("CONTENT.POLICY.VERSION_IMMUTABLE", ErrorCategory.UNPROCESSABLE),
    POLICY_TYPE_ACTIVE_EXISTS("CONTENT.POLICY.TYPE_ACTIVE_EXISTS", ErrorCategory.CONFLICT),
    FAQ_GROUP_NOT_FOUND("CONTENT.FAQ.GROUP_NOT_FOUND", ErrorCategory.NOT_FOUND),
    BULK_TOO_LARGE("CONTENT.BULK.TOO_LARGE", ErrorCategory.VALIDATION),
    MEDIA_NOT_FOUND("MEDIA.NOT_FOUND", ErrorCategory.NOT_FOUND),
    MEDIA_TYPE_NOT_ALLOWED("MEDIA.TYPE_NOT_ALLOWED", ErrorCategory.VALIDATION),
    MEDIA_TOO_LARGE("MEDIA.TOO_LARGE", ErrorCategory.PAYLOAD_TOO_LARGE),
    MEDIA_QUOTA_EXCEEDED("MEDIA.QUOTA_EXCEEDED", ErrorCategory.PAYLOAD_TOO_LARGE),
    MEDIA_REFERENCED("MEDIA.REFERENCED", ErrorCategory.CONFLICT),
    MEDIA_FOLDER_NOT_EMPTY("MEDIA.FOLDER.NOT_EMPTY", ErrorCategory.CONFLICT),
    MEDIA_UNREADABLE("MEDIA.UNREADABLE", ErrorCategory.VALIDATION),
    MEDIA_STORAGE_FAILED("MEDIA.STORAGE_FAILED", ErrorCategory.STORAGE);

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
