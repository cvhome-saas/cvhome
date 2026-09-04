package com.asrevo.cvhome.cua.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.asrevo.cvhome.sso.domain.IdpType;
import com.asrevo.cvhome.sso.dto.PublicIdpDto;
import com.asrevo.cvhome.sso.idp.IdentityProviderService;
import com.asrevo.cvhome.sso.idp.IdpPreset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * What a storefront's sign-in page is told about its social logins.
 *
 * <p>
 * Only the providers marked visible for login are offered, and only their alias and display name travel — never
 * the client id, the secret's presence, or the endpoints, all of which the merchant-facing DTO carries. The
 * storefront page is public, so anything this returns is public.
 * </p>
 */
class SocialLoginConfigServiceTest {

    private static final String GOOGLE_ALIAS = "google";
    private static final String GOOGLE_NAME = "Google";
    private static final String GOOGLE_URL = "https://accounts.google.com";

    private final IdentityProviderService providers = Mockito.mock(IdentityProviderService.class);
    private final SocialLoginConfigService service = new SocialLoginConfigService(providers);

    @Test
    void eachVisibleProviderBecomesOneEntryKeyedByItsAlias() {
        when(providers.visibleForLogin()).thenReturn(List.of(
                new PublicIdpDto(GOOGLE_ALIAS, GOOGLE_NAME, IdpPreset.GOOGLE, IdpType.OIDC, GOOGLE_URL),
                new PublicIdpDto("apple", "Apple", IdpPreset.APPLE, IdpType.OIDC, "https://appleid.apple.com")));

        assertThat(service.enabledLogins())
                .extracting(it -> "%s/%s/%s".formatted(it.providerId(), it.name(), it.registrationId()))
                .containsExactly("google/Google/google", "apple/Apple/apple");
    }

    @Test
    void aStoreWithNoSocialLoginsOffersNoneRatherThanFailing() {
        when(providers.visibleForLogin()).thenReturn(List.of());

        assertThat(service.enabledLogins()).isEmpty();
    }

    @Test
    void nothingBeyondTheAliasAndNameLeavesTheServer() {
        when(providers.visibleForLogin()).thenReturn(List.of(
                new PublicIdpDto(GOOGLE_ALIAS, GOOGLE_NAME, IdpPreset.GOOGLE, IdpType.OIDC, GOOGLE_URL)));

        // The storefront page is public; the authorization URL is the server's to build, not the page's to hold.
        assertThat(service.enabledLogins().getFirst().toString()).doesNotContain("accounts.google.com");
    }
}
