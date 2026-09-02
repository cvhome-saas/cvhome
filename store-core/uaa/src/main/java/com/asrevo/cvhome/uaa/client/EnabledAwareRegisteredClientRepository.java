package com.asrevo.cvhome.uaa.client;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import com.asrevo.cvhome.uaa.repo.ClientExtensionRepository;

/**
 * The registry the protocol endpoints see: a disabled client is not there.
 *
 * <p>
 * Only {@link #findByClientId} filters. That is the lookup every authentication starts from, so a disabled client gets
 * {@code invalid_client} at the token endpoint and an unknown-client error at authorization. {@link #findById} stays
 * unfiltered on purpose: {@code JdbcOAuth2AuthorizationService}'s row mapper calls it for every stored authorization
 * and throws on {@code null}, which would turn "disabled" into "cannot list or revoke its tokens" — the one thing an
 * operator disabling a client most wants to do next.
 * </p>
 */
public class EnabledAwareRegisteredClientRepository implements RegisteredClientRepository {

    private final RegisteredClientRepository delegate;

    private final ClientExtensionRepository extensions;

    public EnabledAwareRegisteredClientRepository(RegisteredClientRepository delegate, ClientExtensionRepository extensions) {
        this.delegate = delegate;
        this.extensions = extensions;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        delegate.save(registeredClient);
    }

    @Override
    public RegisteredClient findById(String id) {
        return delegate.findById(id);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        RegisteredClient client = delegate.findByClientId(clientId);
        if (client == null) {
            return null;
        }
        boolean disabled = extensions.findById(client.getId()).map(extension -> !extension.isEnabled()).orElse(false);
        return disabled ? null : client;
    }

}
