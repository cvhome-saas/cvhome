package com.asrevo.cvhome.content.model.storefront;

import java.util.List;

/**
 * The layout the storefront renders: hidden sections dropped, text flattened to the served locale (with
 * fallback per field), and every {@code mediaId} prop joined by a {@code mediaUrl} sibling so the renderer
 * never calls back for asset addresses.
 */
public record StorefrontLayout(String page, String servedLocale, List<StorefrontLayoutSection> sections) {

}
