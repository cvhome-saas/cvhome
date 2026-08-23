package com.asrevo.cvhome.content.service;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.support.HtmlSanitizer;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlSanitizerTest {

    @Test
    void stripsScriptsAndEventHandlersButKeepsFormatting() {
        String clean = HtmlSanitizer.clean(
                "<h2 onclick=\"x()\">Hi</h2><script>alert(1)</script><p dir=\"rtl\">مرحبا <a href=\"javascript:x\">a</a>"
                        + " <a href=\"https://a.b/c\" target=\"_blank\">ok</a></p>");
        assertThat(clean).contains("<h2>Hi</h2>").contains("dir=\"rtl\"").contains("href=\"https://a.b/c\"")
                .doesNotContain("script").doesNotContain("onclick").doesNotContain("javascript:");
    }

    @Test
    void keepsImagesFromHttpSources() {
        String clean = HtmlSanitizer.clean("<figure><img src=\"http://localhost:9000/b/x.png\" alt=\"x\"></figure>");
        assertThat(clean).contains("<img").contains("src=\"http://localhost:9000/b/x.png\"");
    }

    @Test
    void passesNullAndBlankThrough() {
        assertThat(HtmlSanitizer.clean(null)).isNull();
        assertThat(HtmlSanitizer.clean("  ")).isEqualTo("  ");
    }

}
