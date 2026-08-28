package com.asrevo.cvhome.content.model.banner;

/**
 * The JSON {@code meta} column of a BANNER row.
 */
public record BannerMeta(BannerTarget target, BannerArtwork artwork, BannerTheme theme, boolean loggedInOnly) {
}
