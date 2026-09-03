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
 * <li>{@code MULTI} throws. With thousands of realms a fallback is a guess, and a guess here reads or writes one
 * store's data under another store's request. Failing the operation is the only safe answer.</li>
 * </ul>
 */
public class SsoTenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

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
        return RealmContext.require().getId();
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
