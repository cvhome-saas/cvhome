package com.asrevo.cvhome.content.support;

import org.jsoup.Jsoup;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

/**
 * Server-side allow-list for every rich-text body written through the API. Sanitised on write, never on read, so
 * what the storefront renders is exactly what is stored.
 */
public final class HtmlSanitizer {

    private static final Safelist SAFELIST = Safelist.relaxed()
            .addTags("figure", "figcaption", "section", "hr", "s", "mark")
            .addAttributes(":all", "dir", "class", "id")
            .addAttributes("a", "target", "rel")
            .addAttributes("img", "loading", "width", "height")
            .addProtocols("a", "href", "http", "https", "mailto", "tel")
            .addProtocols("img", "src", "http", "https")
            .preserveRelativeLinks(true);

    private static final Cleaner CLEANER = new Cleaner(SAFELIST);

    private HtmlSanitizer() {
    }

    public static String clean(String html) {
        if (html == null || html.isBlank()) {
            return html;
        }
        var dirty = Jsoup.parseBodyFragment(html);
        var clean = CLEANER.clean(dirty);
        clean.outputSettings().prettyPrint(false);
        return clean.body().html();
    }

}
