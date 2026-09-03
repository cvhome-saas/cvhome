package com.asrevo.cvhome.uaa.client;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

/**
 * A registry holding exactly one client — the view a grace-window authentication hands to Spring's own provider, so
 * that provider matches the presented secret against the retired hash without uaa re-implementing its checks.
 *
 * <p>
 * {@link #save} is a no-op on purpose. The stock provider saves when the encoder wants to upgrade a hash, and the hash
 * on this view is a <em>retired</em> one: writing it back would put the old secret on the registration.
 * </p>
 */
final class SingleClientRepository implements RegisteredClientRepository {

    private final RegisteredClient client;

    SingleClientRepository(RegisteredClient client) {
        this.client = client;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        // Deliberately nothing: see the class comment.
    }

    @Override
    public RegisteredClient findById(String id) {
        return client.getId().equals(id) ? client : null;
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return client.getClientId().equals(clientId) ? client : null;
    }

}
