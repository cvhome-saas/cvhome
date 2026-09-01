package com.asrevo.cvhome.content.model;

/**
 * Where a banner renders on the storefront, with the number of banners that may be live in it at once.
 * HERO and CAROUSEL retired with the SECTION flow: the home hero's slides live inline in the page
 * layout document now, so banners remain only for the announcement STRIP and category COLLECTION art.
 */
public enum BannerPlacement {

    COLLECTION(1), STRIP(1);

    private final int capacity;

    BannerPlacement(int capacity) {
        this.capacity = capacity;
    }

    public int capacity() {
        return capacity;
    }

}
