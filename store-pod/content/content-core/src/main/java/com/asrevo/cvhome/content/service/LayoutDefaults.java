package com.asrevo.cvhome.content.service;

import java.util.List;
import java.util.Map;

import com.asrevo.cvhome.content.model.layout.LayoutDocument;
import com.asrevo.cvhome.content.model.layout.LayoutSection;
import com.asrevo.cvhome.content.model.layout.LayoutStyle;
import com.asrevo.cvhome.content.model.layout.LayoutVisibility;
import com.asrevo.cvhome.content.model.layout.PageKind;

/**
 * The starter home: what a brand-new store's page is, before anyone opens the builder. Designed to look
 * complete with zero merchant data — the hero is a text slide, and the data-driven sections (categories,
 * products) collapse on the storefront while they are empty and grow in as the catalogue fills.
 */
public final class LayoutDefaults {

    private static final LayoutStyle CONTENT_MD = new LayoutStyle("md", "content", "default");

    private static final LayoutVisibility VISIBLE = new LayoutVisibility(false, null);

    private LayoutDefaults() {
    }

    public static LayoutDocument starterHome() {
        return new LayoutDocument(LayoutDocument.CURRENT_SCHEMA_VERSION, PageKind.HOME, List.of(
                new LayoutSection("sec-start-hero", "hero", "minimal", Map.of("height", "md"), null,
                        Map.of("heading", Map.of("en", "Welcome to our store", "ar", "مرحبًا بكم في متجرنا"),
                                "subheading", Map.of("en", "Discover what's new", "ar", "اكتشف الجديد")),
                        new LayoutStyle("lg", "full", "default"), VISIBLE, null),
                new LayoutSection("sec-start-categories", "categories", "grid", Map.of("limit", 6), null,
                        Map.of("title", Map.of("en", "Shop by category", "ar", "تسوق حسب الفئة")),
                        CONTENT_MD, VISIBLE, null),
                new LayoutSection("sec-start-featured", "products", "grid",
                        Map.of("source", Map.of("type", "group", "code", "FEATURED_ITEMS"), "limit", 8), null,
                        Map.of("title", Map.of("en", "Featured", "ar", "مختاراتنا")),
                        CONTENT_MD, VISIBLE, null),
                new LayoutSection("sec-start-newest", "products", "rail",
                        Map.of("source", Map.of("type", "newest"), "limit", 8), null,
                        Map.of("title", Map.of("en", "New arrivals", "ar", "وصل حديثًا")),
                        CONTENT_MD, VISIBLE, null),
                new LayoutSection("sec-start-welcome", "richtext", "centered", Map.of(), null,
                        Map.of("body", Map.of(
                                "en", "<p>We hand-pick every product we sell. Questions? We are one message away.</p>",
                                "ar", "<p>نختار كل منتج نبيعه بعناية. لديك سؤال؟ نحن على بُعد رسالة واحدة.</p>")),
                        CONTENT_MD, VISIBLE, null),
                new LayoutSection("sec-start-newsletter", "newsletter", "inline", Map.of(), null,
                        Map.of("heading", Map.of("en", "Stay in the loop", "ar", "ابقَ على اطلاع")),
                        CONTENT_MD, VISIBLE, null)));
    }

}
