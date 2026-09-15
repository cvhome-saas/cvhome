package com.asrevo.cvhome.commons.domain;

/**
 * An entity that belongs to one store, and says which.
 *
 * <p>
 * The cache library drops a store's cached reads when one of that store's entities commits, and it asks the entity
 * itself rather than keeping a table of entity types: a description or an option value answers through its owner,
 * so a new entity in a cached area implements this and needs no other registration. An entity that cannot say
 * (a shared reference row) returns {@code null}, and nothing is evicted for it.
 * </p>
 */
public interface StoreScoped {

    /** The store this row belongs to, or {@code null} when it belongs to none. */
    StoreMerchantId scopedStore();

}
