package com.asrevo.cvhome.content.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringsTest {

    private static final String ABC = "abc";

    private static final String SPACES = "   ";

    @Test
    void blankCoversNullEmptyAndWhitespace() {
        assertThat(Strings.blank(null)).isTrue();
        assertThat(Strings.blank("")).isTrue();
        assertThat(Strings.blank(SPACES)).isTrue();
        assertThat(Strings.blank("x")).isFalse();
    }

    @Test
    void trimToNullCollapsesWhitespaceOnlyInput() {
        assertThat(Strings.trimToNull(null)).isNull();
        assertThat(Strings.trimToNull(SPACES)).isNull();
        assertThat(Strings.trimToNull("  hi  ")).isEqualTo("hi");
    }

    @Test
    void abbreviateCutsOnlyWhatIsTooLong() {
        assertThat(Strings.abbreviate(null, 5)).isNull();
        assertThat(Strings.abbreviate(ABC, 5)).isEqualTo(ABC);
        assertThat(Strings.abbreviate("abcdef", 3)).isEqualTo(ABC);
    }

}
