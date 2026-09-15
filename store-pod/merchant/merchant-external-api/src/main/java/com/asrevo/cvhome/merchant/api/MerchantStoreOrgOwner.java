package com.asrevo.cvhome.merchant.api;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.s2s.services.StoreOrgOwnerRetriever;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Which organization owns a store, asked of the pod's own store registry.
 *
 * <p>
 * It answers the one question an org admin's token cannot: the token names the organization the person
 * administers, and says nothing about who owns the store named in the query parameter — while every store on the
 * pod is one query parameter away. {@code merchant} holds that mapping, in a column that is
 * {@code updatable = false}, so a store never changes hands and the answer is worth caching.
 * </p>
 *
 * <p>
 * A failure answers null rather than throwing, and the caller reads that as a refusal. An authorization check
 * that cannot be made has not passed, and a store registry that is briefly unreachable must not become a way in.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class MerchantStoreOrgOwner implements StoreOrgOwnerRetriever {

    private final ExternalMerchantStoreService stores;

    @Override
    public ManagerOrgId owner(StoreMerchantId store) {
        if (store == null) {
            return null;
        }
        return lookUp(store);
    }

    private ManagerOrgId lookUp(StoreMerchantId store) {
        try {
            ReadableMerchantStore found = stores.getStore(store);
            String org = found == null ? null : found.getOrg();
            return org == null || org.isBlank() ? null : new ManagerOrgId(org);
        } catch (RuntimeException unreachable) {
            log.warn("Could not establish which organization owns store {}: {}", store, unreachable.getMessage());
            return null;
        }
    }

}
