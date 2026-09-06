package com.asrevo.cvhome.s2s.config.internal;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import com.asrevo.cvhome.commons.domain.LanguageCode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A {@code LanguageCode} controller argument comes from the {@code lang} query parameter, defaulting when absent.
 */
class ServletLanguageCodeArgumentResolverTest {

    private static final String ARABIC = "ar";

    private final ServletLanguageCodeArgumentResolver resolver = new ServletLanguageCodeArgumentResolver();

    @SuppressWarnings("unused")
    private static void endpoint(LanguageCode language, String other) {
    }

    private static MethodParameter parameter(int index) throws NoSuchMethodException {
        return new MethodParameter(
                ServletLanguageCodeArgumentResolverTest.class.getDeclaredMethod("endpoint", LanguageCode.class,
                        String.class), index);
    }

    @Test
    void supportsOnlyLanguageCodeParameters() throws Exception {
        assertThat(resolver.supportsParameter(parameter(0))).isTrue();
        assertThat(resolver.supportsParameter(parameter(1))).isFalse();
    }

    @Test
    void resolvesTheLangParameterOrTheDefault() throws Exception {
        MockHttpServletRequest withLang = new MockHttpServletRequest();
        withLang.setParameter("lang", ARABIC);
        MockHttpServletRequest without = new MockHttpServletRequest();

        assertThat(resolver.resolveArgument(parameter(0), null, new ServletWebRequest(withLang), null))
                .isEqualTo(new LanguageCode(ARABIC));
        assertThat(resolver.resolveArgument(parameter(0), null, new ServletWebRequest(without), null))
                .isEqualTo(LanguageCode.defaultLanguage());
    }

}
