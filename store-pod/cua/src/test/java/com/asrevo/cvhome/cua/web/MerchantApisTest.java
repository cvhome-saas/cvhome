package com.asrevo.cvhome.cua.web;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.service.SocialLoginConfigService;
import com.asrevo.cvhome.sso.idp.IdentityProviderService;
import com.asrevo.cvhome.sso.service.AdminService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The merchant-facing half of cua: shopper administration and identity providers.
 *
 * <p>
 * These endpoints take a {@code StoreMerchantId} and never pass it on, which reads like a bug and is not. cua runs
 * one realm per store, and the realm is resolved from the request before any of this code runs — so the store is
 * already the scope, and every {@code AdminService} call is implicitly inside it. The parameter is there for the
 * {@code @PreAuthorize} expression, which is the only thing that needs to name the store. That makes the gate the
 * whole of the tenant check on this controller, so every method is asserted to carry one.
 * </p>
 */
class MerchantApisTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final LanguageCode ENGLISH = new LanguageCode("en");
    private static final UUID USER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String SESSION = "session-1";
    private static final String CUA_PERMISSION = "'StoreMerchantId','STORE-POD.CUA.*'";

    private final AdminService shoppers = Mockito.mock(AdminService.class);
    private final IdentityProviderService providers = Mockito.mock(IdentityProviderService.class);
    private final SocialLoginConfigService socialLogins = Mockito.mock(SocialLoginConfigService.class);

    private final MerchantShopperController shopperApi = new MerchantShopperController(shoppers);
    private final MerchantIdentityProviderController providerApi =
            new MerchantIdentityProviderController(providers);
    private final PublicSocialLoginController publicApi = new PublicSocialLoginController(socialLogins);

    @Test
    void theShopperReadsGoThroughTheRealmScopedAdminService() throws Exception {
        shopperApi.list(STORE, "q", null, PageRequest.of(0, 20));
        shopperApi.counts(STORE);
        shopperApi.get(STORE, USER);
        shopperApi.sessions(STORE, USER);

        verify(shoppers).getUsers(any(), any());
        verify(shoppers).counts();
        verify(shoppers).getUser(USER);
        verify(shoppers).listSessions(USER);
    }

    @Test
    void theShopperMutationsAllAddressOneUserById() throws Exception {
        shopperApi.disable(STORE, USER);
        shopperApi.enable(STORE, USER);
        shopperApi.unlock(STORE, USER);
        shopperApi.delete(STORE, USER);
        shopperApi.revokeSession(STORE, USER, SESSION);

        verify(shoppers).disableUser(USER);
        verify(shoppers).enableUser(USER);
        verify(shoppers).unlock(USER);
        verify(shoppers).delete(USER);
        verify(shoppers).revokeSession(USER, SESSION);
    }

    @Test
    void revokingEverySessionAnswersWithHowManyWentSoTheConsoleCanSayIt() throws Exception {
        when(shoppers.revokeSessions(USER)).thenReturn(3);

        assertThat(shopperApi.revokeSessions(STORE, USER)).containsEntry("revoked", 3);
    }

    @Test
    void theIdentityProviderEndpointsDelegateAndThePresetsComeFromTheCatalogue() throws Exception {
        providerApi.list(STORE);
        providerApi.get(STORE, USER);
        providerApi.create(STORE, null);
        providerApi.update(STORE, USER, null);
        providerApi.delete(STORE, USER);
        providerApi.enable(STORE, USER);
        providerApi.disable(STORE, USER);

        assertThat(providerApi.presets(STORE)).isNotEmpty();
        verify(providers).list();
        verify(providers).get(USER);
        verify(providers).create(null);
        verify(providers).update(USER, null);
        verify(providers).delete(USER);
        verify(providers).setEnabled(USER, true);
        verify(providers).setEnabled(USER, false);
    }

    @Test
    void theStorefrontsSocialLoginListIsPublicBecauseAShopperHasNoTokenYet() {
        when(socialLogins.enabledLogins()).thenReturn(List.of());

        assertThat(publicApi.enabledLogins(STORE, ENGLISH)).isEmpty();
        assertThat(Stream.of(PublicSocialLoginController.class.getDeclaredMethods())
                .anyMatch(m -> m.isAnnotationPresent(PreAuthorize.class))).isFalse();
    }

    private static Stream<Method> merchantEndpoints() {
        return Stream.of(MerchantShopperController.class, MerchantIdentityProviderController.class)
                .flatMap(type -> Stream.of(type.getDeclaredMethods()))
                .filter(m -> m.getParameterCount() > 0 && m.getParameterTypes()[0] == StoreMerchantId.class)
                .sorted((a, b) -> (a.getDeclaringClass().getSimpleName() + a.getName())
                        .compareTo(b.getDeclaringClass().getSimpleName() + b.getName()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("merchantEndpoints")
    void everyMerchantEndpointCarriesTheGateThatIsItsOnlyTenantCheck(Method endpoint) {
        PreAuthorize gate = endpoint.getAnnotation(PreAuthorize.class);

        assertThat(gate).as("%s.%s has no @PreAuthorize", endpoint.getDeclaringClass().getSimpleName(),
                endpoint.getName()).isNotNull();
        assertThat(gate.value()).contains(CUA_PERMISSION);
    }
}
