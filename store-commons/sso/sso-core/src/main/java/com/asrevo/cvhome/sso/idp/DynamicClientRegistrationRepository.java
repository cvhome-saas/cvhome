package com.asrevo.cvhome.sso.idp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import com.asrevo.cvhome.errors.UncheckedBaseException;
import com.asrevo.cvhome.sso.repo.IdentityProviderRepository;
import com.asrevo.cvhome.uaa.errors.IdpConfigInvalidException;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring's registrations: the ones in configuration first (the platform's own {@code uaa} and {@code s2s} entries),
 * then the identity providers in the database by alias — cua's {@code DynamicClientRegistrationRepository} shape.
 *
 * <p>
 * A built registration is cached until the provider is written: building one may discover an issuer over the network,
 * and a sign-in must not wait on that twice. A disabled provider is not here at all, so
 * {@code /oauth2/authorization/{alias}} for it is a plain "no such registration".
 * </p>
 */
@Slf4j
public class DynamicClientRegistrationRepository implements ClientRegistrationRepository {

    private final Map<String, ClientRegistration> fromProperties;

    private final IdentityProviderRepository providers;

    private final ClientRegistrationFactory factory;

    private final Map<String, ClientRegistration> cache = new ConcurrentHashMap<>();

    public DynamicClientRegistrationRepository(Map<String, ClientRegistration> fromProperties,
                                               IdentityProviderRepository providers, ClientRegistrationFactory factory) {
        this.fromProperties = Map.copyOf(fromProperties);
        this.providers = providers;
        this.factory = factory;
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        ClientRegistration configured = fromProperties.get(registrationId);
        if (configured != null) {
            return configured;
        }
        ClientRegistration cached = cache.get(registrationId);
        if (cached != null) {
            return cached;
        }
        return providers.findByAlias(registrationId).filter(p -> p.isEnabled()).map(p -> {
            try {
                ClientRegistration built = factory.build(p);
                cache.put(registrationId, built);
                return built;
            } catch (IdpConfigInvalidException e) {
                log.error("Identity provider {} cannot be registered: {}", registrationId, e.getMessage());
                throw new UncheckedBaseException(e);
            }
        }).orElse(null);
    }

    /** Forgets what was built for the alias; the next lookup rebuilds it. Called by every provider write. */
    public void evict(String alias) {
        cache.remove(alias);
    }

}
