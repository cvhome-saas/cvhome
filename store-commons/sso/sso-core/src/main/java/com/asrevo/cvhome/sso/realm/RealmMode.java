package com.asrevo.cvhome.sso.realm;

/**
 * How many user pools a deployment of the SSO server serves.
 *
 * <p>
 * The one axis on which uaa and cua genuinely differ. Everything else they do — users, roles, audit, keys,
 * identity providers, lockout, sessions — is the same code in {@code sso-core}; the mode decides whether that
 * code sees one realm or thousands.
 * </p>
 */
public enum RealmMode {

    /**
     * One fixed realm for the life of the deployment,
     * {@link com.asrevo.cvhome.commons.domain.RealmId#PLATFORM}. uaa's mode, and it stays uaa's mode: the
     * platform's staff and service accounts are a single pool, and there is no realm selector anywhere in its
     * API or console.
     */
    SINGLE,

    /**
     * One realm per store, resolved per request. cua's mode. The realm id is the store id, which is what lets one
     * email address be two different shoppers in two different stores.
     */
    MULTI

}
