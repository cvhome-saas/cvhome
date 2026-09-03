package com.asrevo.cvhome.uaa.client;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.uaa.errors.InvalidRedirectUriException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Plain http only where the realm says so, no wildcards, no fragments, native schemes allowed. */
class RedirectUriRulesTest {

    private static final String REASON = "reason";

    private final RedirectUriRules rules = new RedirectUriRules(new ClientsProperties(List.of("localhost", ".gateway.com")));

    @Test
    void httpsAndLocalHttpAndNativeSchemesPass() {
        assertThatCode(() -> rules.validate(Set.of("https://app.example/cb", "http://localhost:4200/cb",
                "http://console.gateway.com:8000/login/oauth2/code/uaa", "com.example.app:/callback")))
                .doesNotThrowAnyException();
    }

    @Test
    void plainHttpElsewhereIsRefused() {
        assertThatThrownBy(() -> rules.validate(Set.of("http://app.example/cb")))
                .isInstanceOf(InvalidRedirectUriException.class)
                .extracting(e -> ((InvalidRedirectUriException) e).params().get(REASON)).isEqualTo(RedirectUriRules.PLAIN_HTTP);
    }

    @Test
    void wildcardsAndFragmentsAndRelativePathsAreRefused() {
        assertThatThrownBy(() -> rules.validate(Set.of("https://*.example/cb")))
                .extracting(e -> ((InvalidRedirectUriException) e).params().get(REASON)).isEqualTo(RedirectUriRules.WILDCARD);
        assertThatThrownBy(() -> rules.validate(Set.of("https://app.example/cb#frag")))
                .extracting(e -> ((InvalidRedirectUriException) e).params().get(REASON)).isEqualTo(RedirectUriRules.FRAGMENT);
        assertThatThrownBy(() -> rules.validate(Set.of("/cb")))
                .extracting(e -> ((InvalidRedirectUriException) e).params().get(REASON)).isEqualTo(RedirectUriRules.NOT_ABSOLUTE);
    }

}
