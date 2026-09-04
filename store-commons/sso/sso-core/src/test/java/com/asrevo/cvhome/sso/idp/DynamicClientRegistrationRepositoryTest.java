package com.asrevo.cvhome.sso.idp;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.sso.domain.AccountLinking;
import com.asrevo.cvhome.sso.domain.IdentityProvider;
import com.asrevo.cvhome.sso.realm.RealmContext;
import com.asrevo.cvhome.sso.realm.RealmMode;
import com.asrevo.cvhome.sso.realm.SsoRealmProperties;
import com.asrevo.cvhome.sso.realm.SsoTenantIdentifierResolver;
import com.asrevo.cvhome.sso.repo.IdentityProviderRepository;
import com.asrevo.cvhome.sso.support.FakeCrypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Configuration first, then the database once per alias until evicted; a disabled provider is not there. */
class DynamicClientRegistrationRepositoryTest {

    private static final String CONFIGURED_CLIENT = "c";

    private static final String CONFIGURED = "uaa";

    private final IdentityProviderRepository providers = mock(IdentityProviderRepository.class);

    /** SINGLE mode: the resolver answers with the one realm, so the cache key is stable. */
    private final SsoTenantIdentifierResolver realms = new SsoTenantIdentifierResolver(singleRealm());

    private final IdentityProviderMapper mapper = new IdentityProviderMapper(new FakeCrypto((byte) 0x07));

    private final DynamicClientRegistrationRepository repository = new DynamicClientRegistrationRepository(
            Map.of(CONFIGURED, ClientRegistration.withRegistrationId(CONFIGURED).clientId(CONFIGURED_CLIENT)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS).tokenUri("https://t").build()),
            providers, new ClientRegistrationFactory(mapper), realms);

    private static SsoRealmProperties singleRealm() {
        SsoRealmProperties properties = new SsoRealmProperties();
        properties.setMode(RealmMode.SINGLE);
        return properties;
    }

    private IdentityProvider stored(boolean enabled) {
        IdentityProvider p = mapper.toNewEntity(IdpFixtures.request(IdpPreset.GENERIC_OIDC, AccountLinking.CONFIRM, true), 0,
                java.time.Instant.EPOCH);
        p.setEnabled(enabled);
        return p;
    }

    @Test
    void configuredRegistrationsWinAndDatabaseOnesAreCachedUntilEvicted() {
        when(providers.findByAlias(IdpFixtures.ALIAS)).thenReturn(Optional.of(stored(true)));

        assertThat(repository.findByRegistrationId(CONFIGURED).getClientId()).isEqualTo(CONFIGURED_CLIENT);
        ClientRegistration first = repository.findByRegistrationId(IdpFixtures.ALIAS);
        ClientRegistration second = repository.findByRegistrationId(IdpFixtures.ALIAS);

        assertThat(first).isSameAs(second);
        assertThat(first.getClientId()).isEqualTo(IdpFixtures.CLIENT_ID);
        assertThat(first.getRedirectUri()).isEqualTo(ClientRegistrationFactory.REDIRECT_TEMPLATE);
        assertThat(first.getProviderDetails().getJwkSetUri()).endsWith("/jwks");
        verify(providers, times(1)).findByAlias(IdpFixtures.ALIAS);

        repository.evict(IdpFixtures.ALIAS);
        assertThat(repository.findByRegistrationId(IdpFixtures.ALIAS)).isNotSameAs(first);
    }

    /**
     * The leak this key exists to stop: two stores both call their provider "google", and a cache keyed on the
     * alias alone would hand the first store's client id, secret and redirect to the second.
     */
    @Test
    void oneRealmsProviderIsNeverServedToAnother() {
        SsoTenantIdentifierResolver multi = new SsoTenantIdentifierResolver(multiRealm());
        DynamicClientRegistrationRepository shared = new DynamicClientRegistrationRepository(
                Map.of(), providers, new ClientRegistrationFactory(mapper), multi);
        when(providers.findByAlias(IdpFixtures.ALIAS)).thenReturn(Optional.of(stored(true)));

        ClientRegistration inA = RealmContext.callIn(RealmId.of("store-a"),
                () -> shared.findByRegistrationId(IdpFixtures.ALIAS));
        ClientRegistration inB = RealmContext.callIn(RealmId.of("store-b"),
                () -> shared.findByRegistrationId(IdpFixtures.ALIAS));

        assertThat(inA).isNotSameAs(inB);
        // Built once per realm rather than once in total: the second store did not read the first store's entry.
        verify(providers, times(2)).findByAlias(IdpFixtures.ALIAS);
    }

    private static SsoRealmProperties multiRealm() {
        SsoRealmProperties properties = new SsoRealmProperties();
        properties.setMode(RealmMode.MULTI);
        return properties;
    }

    @Test
    void aDisabledOrUnknownProviderIsNoRegistration() {
        when(providers.findByAlias(IdpFixtures.ALIAS)).thenReturn(Optional.of(stored(false)));

        assertThat(repository.findByRegistrationId(IdpFixtures.ALIAS)).isNull();
        assertThat(repository.findByRegistrationId("ghost")).isNull();
    }

}
