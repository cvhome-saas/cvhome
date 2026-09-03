package com.asrevo.cvhome.sso.realm;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

import com.asrevo.cvhome.commons.domain.RealmId;

/**
 * Tells Hibernate which realm the current work belongs to, so that {@code @TenantId} can filter every query and
 * populate every insert without a single {@code where realm_id = ?} being written by hand.
 *
 * <p>
 * The two modes answer a missing realm very differently, and the asymmetry is the point:
 * </p>
 * <ul>
 * <li>{@code SINGLE} falls back to the one realm the deployment serves. There is nothing else it could mean, and
 * it lets uaa's schedulers — key rotation, audit retention — run outside a request as they always have.</li>
 * <li>{@code MULTI} answers {@link #NO_REALM}, a realm id no store can have, so a query outside a request matches
 * nothing instead of matching everything. With thousands of realms any other fallback is a guess, and a guess
 * here reads one store's data under another store's request.</li>
 * </ul>
 *
 * <p>
 * It answers a sentinel rather than throwing because Hibernate calls this while it builds queries at startup,
 * long before any request exists — throwing there stops the application from booting at all. Application code
 * that needs a realm and has none should call {@link RealmContext#require()}, which does throw, and says so.
 * </p>
 */
public class SsoTenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    /**
     * The realm of no store. Not ObjectId hex, so it can never collide with a store id, and not the platform
     * realm either — anything scoped to it matches nothing, and a row that somehow carried it would stand out.
     */
    public static final String NO_REALM = "__no_realm__";

    private final SsoRealmProperties properties;

    public SsoTenantIdentifierResolver(SsoRealmProperties properties) {
        this.properties = properties;
    }

    @Override
    public String resolveCurrentTenantIdentifier() {
        RealmId current = RealmContext.current().orElse(null);
        if (current != null) {
            return current.getId();
        }
        if (properties.single()) {
            return properties.fixedRealm().getId();
        }
        return NO_REALM;
    }

    /**
     * False: a second-level cache entry from one realm must never be served to another, and Hibernate only keeps
     * the tenant out of the cache key when this says the session data is realm-independent.
     */
    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }

}
