package com.asrevo.cvhome.content.model.banner;

/**
 * Where a banner click goes: an internal collection/product/page by handle, or an absolute URL.
 */
public record BannerTarget(Kind kind, String value) {

    public enum Kind {
        COLLECTION, PRODUCT, PAGE, URL
    }

}
