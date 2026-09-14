package com.asrevo.cvhome.cache;

import java.io.Serializable;
import java.util.List;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * The key of a cached storefront read: the store it belongs to, then the rest of the call's arguments in order.
 *
 * <p>
 * Keeping the store apart from the other arguments is what lets {@link EntityCommitCacheEviction} drop one store's
 * entries when that store writes and leave every other tenant's warm. {@code arguments} may hold nulls, so it is a
 * list, not {@link List#of}.
 * </p>
 */
public record StoreScopedKey(StoreMerchantId store, List<Object> arguments) implements Serializable {
}
