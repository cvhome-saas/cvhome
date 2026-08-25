package com.asrevo.cvhome.content.model;

import java.util.Locale;

public enum MediaKind {

    IMAGE, VIDEO, DOCUMENT, ARCHIVE, VECTOR;

    public static MediaKind ofMimeType(String mimeType) {
        String mime = mimeType == null ? "" : mimeType.toLowerCase(Locale.ROOT);
        if ("image/svg+xml".equals(mime)) {
            return VECTOR;
        }
        if (mime.startsWith("image/")) {
            return IMAGE;
        }
        if (mime.startsWith("video/")) {
            return VIDEO;
        }
        if ("application/zip".equals(mime) || "application/x-zip-compressed".equals(mime)) {
            return ARCHIVE;
        }
        return DOCUMENT;
    }

}
