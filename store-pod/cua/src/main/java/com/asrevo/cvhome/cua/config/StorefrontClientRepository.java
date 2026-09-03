package com.asrevo.cvhome.cua.config;

import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.cua.security.StorefrontUrls;
import com.asrevo.cvhome.sso.realm.RealmRegistry;

/**
 * The storefront's OAuth2 client: one per store, and only for a store this pod actually serves.
 *
 * <p>
 * <strong>Stored, with its redirect URIs derived.</strong> The client is a row like uaa's, because an issued
 * authorization carries a foreign key to it — an authorization for a client that does not exist has nowhere to be
 * written. What cannot be a row is the redirect URI: a store is reached on a per-store subdomain and on any number
 * of merchant-owned custom domains, in any of its languages, so the valid set is not a list anyone could write
 * down. That part is derived from the request, using the same origin rule as the login hand-off so the two can
 * never disagree about host or port.
 * </p>
 *
 * <p>
 * <strong>What changed is the existence check.</strong> cua used to answer every {@code client_id} with a freshly
 * built client, so an unknown or mistyped store got a working authorization flow and was only stopped later, by
 * the user lookup finding nobody — defence by accident. The store has to be a realm this pod serves now, or there
 * is no client at all.
 * </p>
 *
 * <p>
 * <strong>And the lifetimes.</strong> Access, refresh <em>and authorization code</em> were all 86400s. A
 * one-day authorization code is far outside RFC 6749's guidance of about ten minutes: a code that leaks through a
 * Referer header, browser history or a log stays usable for a day. PKCE narrows that but does not close it.
 * </p>
 */
public class StorefrontClientRepository implements RegisteredClientRepository {

    private static final String LANG = "lang";

    private final RegisteredClientRepository delegate;

    private final RealmRegistry realms;

    public StorefrontClientRepository(RegisteredClientRepository delegate, RealmRegistry realms) {
        this.delegate = delegate;
        this.realms = realms;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        delegate.save(registeredClient);
    }

    /**
     * By id, unchanged. The authorization store looks clients up this way while reading a row back, and it must
     * find one even where {@link #findByClientId} would refuse — otherwise an operator could not revoke the
     * tokens of a store that has since been disabled.
     */
    @Override
    public @Nullable RegisteredClient findById(String id) {
        return withStorefrontRedirects(delegate.findById(id));
    }

    @Override
    public @Nullable RegisteredClient findByClientId(String clientId) {
        if (Objects.isNull(clientId) || !realms.exists(RealmId.of(clientId))) {
            return null;
        }
        return withStorefrontRedirects(delegate.findByClientId(clientId));
    }

    private @Nullable RegisteredClient withStorefrontRedirects(@Nullable RegisteredClient client) {
        if (Objects.isNull(client)) {
            return null;
        }
        Optional<String> origin = origin(true);
        if (origin.isEmpty()) {
            // No request to derive from — a background read of a stored authorization. The row is enough.
            return client;
        }
        return RegisteredClient.from(client)
                .redirectUris(uris -> {
                    uris.clear();
                    uris.add("%s/callback".formatted(origin.get()));
                })
                .postLogoutRedirectUris(uris -> {
                    uris.clear();
                    uris.add(origin.get());
                })
                .build();
    }

    /**
     * The origin the shopper is on, optionally with their language — the same rule
     * {@link StorefrontUrls#origin} applies to the login redirect.
     */
    private Optional<String> origin(boolean withLanguage) {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return Optional.empty();
        }
        HttpServletRequest request = attributes.getRequest();
        StringBuilder uri = new StringBuilder(StorefrontUrls.origin(request));
        if (withLanguage && Objects.nonNull(request.getParameter(LANG))) {
            uri.append('/').append(request.getParameter(LANG));
        }
        return Optional.of(uri.toString());
    }

}
