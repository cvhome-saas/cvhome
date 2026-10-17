package com.asrevo.cvhome.merchant.reads;

import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.cache.spring.StoreScopedKeyGenerator;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.errors.MerchantStoreNotFoundException;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.merchant.service.facade.merchant.StoreFacade;

import lombok.RequiredArgsConstructor;

/**
 * The store record as its peers and the storefront read it, held per store ({@link MerchantRegions}); a save of
 * the store drops its entries at once on the task that took it. The console's full read stays live.
 */
@Component
@CacheConfig(keyGenerator = StoreScopedKeyGenerator.BEAN)
@RequiredArgsConstructor
public class MerchantStoreReads {

    private final StoreFacade stores;

    /** What a peer service reads: no language, the store's own. */
    @Cacheable(MerchantRegions.Names.STORE)
    public ReadableMerchantStore store(StoreMerchantId store) {
        return stores.getReadableMerchantStoreId(store);
    }

    @Cacheable(MerchantRegions.Names.STORE_BY_LANGUAGE)
    public ReadableMerchantStore store(StoreMerchantId store, LanguageCode language)
            throws MerchantStoreNotFoundException {
        return stores.getByMerchantStoreId(store, language);
    }

    @Cacheable(MerchantRegions.Names.LANGUAGES)
    public List<LanguageCode> languages(StoreMerchantId store) {
        return stores.supportedLanguages(store);
    }
}
