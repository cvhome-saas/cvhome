package com.asrevo.cvhome.content.service;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.support.HtmlSanitizer;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlSanitizerTest {

    private static final String JS_SCHEME = "javascript:";

    @Test
    void stripsScriptsAndEventHandlersButKeepsFormatting() {
        String clean = HtmlSanitizer.clean("""
                <h2 onclick="x()">Hi</h2><script>alert(1)</script><p dir="rtl">مرحبا <a href="javascript:x">a</a> \
                <a href="https://a.b/c" target="_blank">ok</a></p>""");
        assertThat(clean).contains("<h2>Hi</h2>").contains("dir=\"rtl\"").contains("href=\"https://a.b/c\"")
                .doesNotContain("script").doesNotContain("onclick").doesNotContain(JS_SCHEME);
    }

    @Test
    void keepsImagesFromHttpSources() {
        String clean = HtmlSanitizer.clean("<figure><img src=\"http://localhost:9000/b/x.png\" alt=\"x\"></figure>");
        assertThat(clean).contains("<img").contains("src=\"http://localhost:9000/b/x.png\"");
    }

    /**
     * A merchant linking to their own pages is the common case — an announcement strip is barely worth having
     * without it — and jsoup drops a relative href unless the fragment is parsed against a base URL.
     */
    @Test
    void keepsLinksToTheStoreItsOwnPages() {
        String clean = HtmlSanitizer.clean(
                "<p>Sale <a href=\"/content/sale\">here</a>, <a href=\"#terms\">terms</a>, "
                        + "<a href=\"mailto:a@b.c\">mail</a>, <a href=\"javascript:x()\">no</a></p>");

        assertThat(clean).contains("href=\"/content/sale\"").contains("href=\"mailto:a@b.c\"")
                .doesNotContain(JS_SCHEME);
    }

    @Test
    void passesNullAndBlankThrough() {
        assertThat(HtmlSanitizer.clean(null)).isNull();
        String blank = "  ";
        assertThat(HtmlSanitizer.clean(blank)).isEqualTo(blank);
    }

}
