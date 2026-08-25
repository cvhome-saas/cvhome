package com.asrevo.cvhome.store.core.modules.cms.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes owned by the CMS asset layer — every way storing, reading or resizing a store's files can fail.
 *
 * <p>
 * All eleven throw sites this replaces were {@code new ServiceException(e)}, so a missing image, an S3 bucket that
 * could not be reached and a resize configured with a negative width were one indistinguishable 500. Splitting them is
 * what lets a seller be told "that file is gone" rather than "something went wrong", and lets an operator tell a
 * genuine outage from a bad upload without reading the stack trace.
 * </p>
 */
public enum CmsErrors implements ErrorCode {

    /**
     * The requested object does not exist under that key — the only condition here that is not our fault.
     */
    ASSET_NOT_FOUND("CMS.ASSET.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * The object exists but could not be fetched or streamed back.
     */
    ASSET_READ_FAILED("CMS.ASSET.READ_FAILED", ErrorCategory.STORAGE),

    /**
     * Storing an object failed, so the caller must assume nothing was written.
     */
    ASSET_UPLOAD_FAILED("CMS.ASSET.UPLOAD_FAILED", ErrorCategory.STORAGE),

    /**
     * Deleting an object or folder failed; the object may still be present.
     */
    ASSET_DELETE_FAILED("CMS.ASSET.DELETE_FAILED", ErrorCategory.STORAGE),

    /**
     * Enumerating the objects under a prefix failed, so a partial listing must not be treated as complete.
     */
    ASSET_LIST_FAILED("CMS.ASSET.LIST_FAILED", ErrorCategory.STORAGE),

    /**
     * The uploaded bytes are not an image any decoder on the classpath recognises. The caller's file is wrong, which is
     * why this is the one image failure that renders as a 400.
     */
    IMAGE_UNREADABLE("CMS.IMAGE.UNREADABLE", ErrorCategory.VALIDATION),

    /**
     * The configured product image dimensions are missing, non-numeric or not positive, so no resize can be attempted.
     */
    IMAGE_SIZE_MISCONFIGURED("CMS.IMAGE.SIZE_MISCONFIGURED", ErrorCategory.STORAGE);

    private final String code;

    private final ErrorCategory category;

    CmsErrors(String code, ErrorCategory category) {
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
