package com.asrevo.cvhome.merchant.api;

import org.springframework.cache.annotation.Cacheable;

import com.asrevo.cvhome.cache.spring.StoreScopedKeyGenerator;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;

/**
 * The store read, cached in front of the merchant client: a store's currency, units, languages and login rule
 * change rarely and are read on every product mapping and every cart call.
 *
 * <p>
 * The one decorator every consumer uses, in the module that owns the contract; a service declares it as its
 * {@code ExternalMerchantStoreService} bean, typed as this class so Spring builds the caching proxy ahead of time
 * (declared as the interface, a native image was never proxied and every product mapping called merchant). Nothing
 * evicts it but the region's time-to-live until the store's own change event arrives; a store that does not exist
 * is not held.
 * </p>
 */
public class CachedMerchantStoreReads implements ExternalMerchantStoreService {

    private final ExternalMerchantStoreService delegate;

    public CachedMerchantStoreReads(ExternalMerchantStoreService delegate) {
        this.delegate = delegate;
    }

    @Override
    @Cacheable(cacheNames = MerchantClientRegions.Names.STORE_CLIENT, keyGenerator = StoreScopedKeyGenerator.BEAN)
    public ReadableMerchantStore getStore(StoreMerchantId store) {
        return delegate.getStore(store);
    }
}
