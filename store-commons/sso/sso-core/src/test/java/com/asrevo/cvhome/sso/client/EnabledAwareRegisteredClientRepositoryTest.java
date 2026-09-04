package com.asrevo.cvhome.sso.client;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import com.asrevo.cvhome.sso.domain.ClientExtension;
import com.asrevo.cvhome.sso.repo.ClientExtensionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** A disabled client vanishes from {@code findByClientId} and nowhere else. */
class EnabledAwareRegisteredClientRepositoryTest {

    private static final String ID = "reg-1";

    private static final String CLIENT_ID = "svc";

    private final RegisteredClientRepository delegate = mock(RegisteredClientRepository.class);

    private final ClientExtensionRepository extensions = mock(ClientExtensionRepository.class);

    private final EnabledAwareRegisteredClientRepository repository = new EnabledAwareRegisteredClientRepository(delegate,
            extensions);

    private final RegisteredClient client = RegisteredClient.withId(ID).clientId(CLIENT_ID).clientSecret("x")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS).build();

    @Test
    void aDisabledClientIsInvisibleByClientIdButNotById() {
        ClientExtension extension = ClientExtension.create(ID, null, Instant.EPOCH);
        extension.disable(Instant.EPOCH, "admin");
        when(delegate.findByClientId(CLIENT_ID)).thenReturn(client);
        when(delegate.findById(ID)).thenReturn(client);
        when(extensions.findById(ID)).thenReturn(Optional.of(extension));

        assertThat(repository.findByClientId(CLIENT_ID)).isNull();
        assertThat(repository.findById(ID)).isSameAs(client);
    }

    @Test
    void anEnabledOrUnknownExtensionPassesThrough() {
        when(delegate.findByClientId(CLIENT_ID)).thenReturn(client);
        when(extensions.findById(ID)).thenReturn(Optional.empty());

        assertThat(repository.findByClientId(CLIENT_ID)).isSameAs(client);
    }

}
