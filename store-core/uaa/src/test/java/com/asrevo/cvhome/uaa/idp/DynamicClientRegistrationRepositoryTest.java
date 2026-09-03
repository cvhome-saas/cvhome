package com.asrevo.cvhome.uaa.idp;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import com.asrevo.cvhome.uaa.domain.AccountLinking;
import com.asrevo.cvhome.uaa.domain.IdentityProvider;
import com.asrevo.cvhome.uaa.repo.IdentityProviderRepository;
import com.asrevo.cvhome.uaa.support.FakeCrypto;

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

    private final IdentityProviderMapper mapper = new IdentityProviderMapper(new FakeCrypto((byte) 0x07));

    private final DynamicClientRegistrationRepository repository = new DynamicClientRegistrationRepository(
            Map.of(CONFIGURED, ClientRegistration.withRegistrationId(CONFIGURED).clientId(CONFIGURED_CLIENT)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS).tokenUri("https://t").build()),
            providers, new ClientRegistrationFactory(mapper));

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

    @Test
    void aDisabledOrUnknownProviderIsNoRegistration() {
        when(providers.findByAlias(IdpFixtures.ALIAS)).thenReturn(Optional.of(stored(false)));

        assertThat(repository.findByRegistrationId(IdpFixtures.ALIAS)).isNull();
        assertThat(repository.findByRegistrationId("ghost")).isNull();
    }

}
