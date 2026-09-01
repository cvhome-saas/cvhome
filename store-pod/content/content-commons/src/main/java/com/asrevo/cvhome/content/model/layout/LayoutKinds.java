package com.asrevo.cvhome.content.model.layout;

import java.util.Set;

/**
 * The section kinds this service accepts. The full catalogue — variants, prop schemas, presets — is owned by
 * the storefront (landing-ui's section catalog) and served to the console from there; the backend only refuses
 * a kind nothing can render, so a typo cannot be published.
 */
public final class LayoutKinds {

    public static final Set<String> KNOWN = Set.of("hero", "products", "categories", "promo", "image", "richtext",
            "faq", "posts", "testimonials", "newsletter", "usp", "video", "brands");

    private LayoutKinds() {
    }

}
