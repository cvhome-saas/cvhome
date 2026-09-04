package com.asrevo.cvhome.sso.realm;

import jakarta.servlet.http.HttpServletRequest;

import com.asrevo.cvhome.commons.domain.RealmId;

/**
 * The {@code SINGLE}-mode resolver: every request belongs to the one realm this deployment serves.
 *
 * <p>
 * uaa's answer, permanently. Its staff and service accounts are a single pool, so there is nothing to resolve and
 * no per-request cost to resolving it.
 * </p>
 */
public class FixedRealmResolver implements RealmResolver {

    private final RealmId realm;

    public FixedRealmResolver(RealmId realm) {
        this.realm = realm;
    }

    @Override
    public RealmId resolve(HttpServletRequest request) {
        return realm;
    }

}
