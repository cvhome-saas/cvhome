package com.asrevo.cvhome.content.model;

/**
 * Where a banner renders on the storefront, with the number of banners that may be live in it at once.
 */
public enum BannerPlacement {

    HERO(1), CAROUSEL(8), COLLECTION(1), STRIP(1);

    private final int capacity;

    BannerPlacement(int capacity) {
        this.capacity = capacity;
    }

    public int capacity() {
        return capacity;
    }

}
