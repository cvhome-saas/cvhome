package com.asrevo.cvhome.commons.domain;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A realm's identifier: the user pool a principal belongs to.
 *
 * <p>
 * <b>Not to be confused with the issuer realm</b> of {@code com.asrevo.cvhome.s2s.jwt.IssuerRealm}, which names
 * <em>which authorization server</em> minted a token ({@code uaa} or {@code cua}) and is what a resource server's
 * trust list is written against. This type names <em>which user pool inside one of those servers</em> a user
 * belongs to. The two are independent: every realm below lives inside exactly one issuer realm.
 * </p>
 *
 * <p>
 * uaa runs with the single fixed realm {@link #PLATFORM} and will keep doing so — its staff and service accounts
 * are one pool. cua runs one realm per store, whose id <em>is</em> the {@link StoreMerchantId}, which is what makes
 * the same email address two different shoppers in two different stores.
 * </p>
 *
 * <p>
 * <b>Deliberately unvalidated</b>, for the same reason {@link StoreMerchantId} is: a realm id is either the
 * {@code PLATFORM} literal or a store id, and rejecting a malformed value belongs at the HTTP edge where the realm
 * is resolved, not in this constructor.
 * </p>
 */
public record RealmId(String id) implements Identifier, Comparable<RealmId> {

    /**
     * The one realm uaa ever has. A literal rather than a store id so it can never collide with one: store ids are
     * ObjectId hex, which this is not.
     */
    public static final RealmId PLATFORM = new RealmId("platform");

    public static RealmId of(String id) {
        return new RealmId(id);
    }

    /**
     * The realm a store's shoppers live in. A store's realm id is its store id — one store, one pool.
     */
    public static RealmId of(StoreMerchantId store) {
        return new RealmId(store.getId());
    }

    @JsonValue
    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public int compareTo(RealmId o) {
        return this.id.compareTo(o.id);
    }

}
