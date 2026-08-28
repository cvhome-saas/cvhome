package com.asrevo.cvhome.content.storage;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An uploaded SVG is a document the storefront serves from its own origin, so anything in it that can run code or
 * phone home has to be gone before it reaches object storage.
 */
class SvgSanitizerTest {

    private static final String EMPTY_SVG = "<svg></svg>";

    private static String clean(String svg) {
        return new String(SvgSanitizer.clean(svg.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    @Test
    void scriptsGoInBothTheirForms() {
        assertThat(clean("<svg><script>alert(1)</script><rect/></svg>")).isEqualTo("<svg><rect/></svg>");
        assertThat(clean("<svg><script src='x.js'/></svg>")).isEqualTo(EMPTY_SVG);
    }

    @Test
    void foreignObjectGoesWithItsContents() {
        assertThat(clean("<svg><foreignObject><iframe/></foreignObject></svg>")).isEqualTo(EMPTY_SVG);
    }

    @Test
    void eventHandlerAttributesAreStripped() {
        assertThat(clean("<svg><rect onclick=\"steal()\" onload='x' fill=\"red\"/></svg>"))
                .doesNotContain("onclick").doesNotContain("onload").contains("fill=\"red\"");
    }

    @Test
    void externalReferencesGoButFragmentsAndInlineDataStay() {
        assertThat(clean("<svg><use xlink:href=\"https://evil.test/x.svg\"/></svg>")).doesNotContain("evil.test");
        assertThat(clean("<svg><use xlink:href=\"#glyph\"/></svg>")).contains("#glyph");
        assertThat(clean("<svg><image href=\"data:image/png;base64,AA\"/></svg>")).contains("data:image/png");
    }

    @Test
    void javascriptUrlsAreDefused() {
        assertThat(clean("<svg><a href=\"#x\" style=\"background:javascript:alert(1)\"/></svg>"))
                .doesNotContain("javascript:");
    }

    @Test
    void anOrdinarySvgSurvivesUntouched() {
        String plain = "<svg viewBox=\"0 0 10 10\"><circle cx=\"5\" cy=\"5\" r=\"4\"/></svg>";

        assertThat(clean(plain)).isEqualTo(plain);
    }

}
