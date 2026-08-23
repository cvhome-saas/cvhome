package com.asrevo.cvhome.tenancy.commons.dto;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * What narrows a store listing. Every field is optional and they are AND-ed.
 *
 * @param pod the pod the store is placed on. Tenancy owns {@code manager_store.pod_id}, so this is the authoritative
 *            answer to "which stores are on this pod" — pod-registry's {@code pod_store_placement} is a copy it
 *            maintains from tenancy's outbox, and one that knows only about stores placed through it.
 */
public record ListManagerStoreQuery(StoreMerchantId id, String name, IdentityId owner, PodId pod) {
}
