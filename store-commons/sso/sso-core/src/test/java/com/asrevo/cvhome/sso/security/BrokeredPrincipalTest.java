package com.asrevo.cvhome.sso.security;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** What the broker holds between the provider's answer and the success handler. */
class BrokeredPrincipalTest {

    private static final String LOGIN = "login";

    private static final String OCTOCAT = "octocat";

    private static final String EMAIL = "email";

    private static final String GITHUB = "github";

    /**
     * The bug this exists for: GitHub's {@code /user} answers with {@code email}, {@code name} and {@code company}
     * set to null for any account that has not filled them in, and {@code Map.copyOf} rejects null values — so
     * brokering a GitHub sign-in threw a NullPointerException before the broker ever saw the attributes.
     */
    @Test
    void keepsTheProvidersNullAttributesInsteadOfThrowing() {
        Map<String, Object> fromGithub = new HashMap<>();
        fromGithub.put(LOGIN, OCTOCAT);
        fromGithub.put(EMAIL, null);
        fromGithub.put("company", null);

        BrokeredPrincipal principal = new BrokeredPrincipal(OCTOCAT, GITHUB, fromGithub, null, null);

        assertThat(principal.getAttributes()).containsEntry(LOGIN, OCTOCAT).containsEntry(EMAIL, null);
    }

    @Test
    void theCopyIsUnmodifiableAndDetachedFromTheCaller() {
        Map<String, Object> mutable = new HashMap<>();
        mutable.put(LOGIN, OCTOCAT);

        BrokeredPrincipal principal = new BrokeredPrincipal(OCTOCAT, GITHUB, mutable, null, null);
        mutable.put(LOGIN, "someone-else");

        assertThat(principal.getAttributes()).containsEntry(LOGIN, OCTOCAT);
        assertThatThrownBy(() -> principal.getAttributes().put("x", "y"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void noAttributesIsAnEmptyMap() {
        assertThat(new BrokeredPrincipal("who", GITHUB, null, null, null).getAttributes()).isEmpty();
    }

}
